package scalapb.compiler

import com.google.protobuf.Message
import scalapb.options.Scalapb
import scalapb.options.Scalapb.MatchType
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FileDescriptor
import scala.jdk.CollectionConverters._
import com.google.protobuf.DescriptorProtos.{FieldOptions, FieldDescriptorProto}
import scalapb.options.Scalapb.FieldTransformation
import com.google.protobuf.DynamicMessage
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import scalapb.options.Scalapb.ScalaPbOptions.AuxFieldOptions
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.ByteString
import java.util.regex.Pattern
import java.util.regex.Matcher

private[compiler] case class ResolvedFieldTransformation(
    whenFields: Map[FieldDescriptor, Any],
    set: scalapb.options.Scalapb.FieldOptions,
    matchType: MatchType,
    extensions: Set[FieldDescriptor]
)

private[compiler] case class ExtensionResolutionContext(
    currentFile: String,
    extensions: Set[FieldDescriptor]
)

private[compiler] object ResolvedFieldTransformation {
  def apply(
      file: FileDescriptor,
      ft: FieldTransformation
  ): ResolvedFieldTransformation = {
    val context =
      ExtensionResolutionContext(
        file.getFullName(),
        FieldTransformations.fieldExtensionsForFile(file)
      )
    if (
      !ft.getSet().getAllFields().keySet().asScala.subsetOf(Set(Scalapb.field.getDescriptor()))
      || !ft
        .getSet()
        .getUnknownFields()
        .asMap()
        .keySet()
        .asScala
        .subsetOf(Set(Scalapb.field.getNumber()))
    ) {
      throw new GeneratorException(
        s"${file.getFullName}: FieldTransformation.set must contain only [scalapb.field] field."
      )
    }
    ResolvedFieldTransformation(
      FieldTransformations.fieldMap(
        FieldDescriptorProto.parseFrom(ft.getWhen.toByteArray()),
        context = context
      ),
      ft.getSet().getExtension(Scalapb.field),
      ft.getMatchType(),
      context.extensions
    )
  }
}

private[compiler] object FieldTransformations {
  def matches(
      currentFile: String,
      input: FieldDescriptorProto,
      transformation: ResolvedFieldTransformation
  ): Boolean = {
    transformation.matchType match {
      case MatchType.CONTAINS =>
        matchContains(
          input,
          transformation.whenFields,
          ExtensionResolutionContext(currentFile, transformation.extensions)
        )
      case MatchType.EXACT    => input == transformation.whenFields
      case MatchType.PRESENCE =>
        matchPresence(
          input,
          transformation.whenFields,
          ExtensionResolutionContext(currentFile, transformation.extensions)
        )
    }
  }

  def matchContains(
      input: Message,
      pattern: Map[FieldDescriptor, Any],
      context: ExtensionResolutionContext
  ): Boolean = {
    pattern.forall { case (fd, v) =>
      if (!fd.isExtension()) {
        if (fd.getType() != Type.MESSAGE)
          if (fd.isRepeated) {
            input.getRepeatedFieldCount(fd) > 0 && input.getField(fd) == v
          } else
            input.hasField(fd) && input.getField(fd) == v
        else {
          input.hasField(fd) && matchContains(
            input.getField(fd).asInstanceOf[Message],
            v.asInstanceOf[Map[FieldDescriptor, Any]],
            context
          )
        }
      } else {
        input.getUnknownFields().hasField(fd.getNumber) &&
        matchContains(
          getExtensionField(input, fd),
          v.asInstanceOf[Map[FieldDescriptor, Any]],
          context
        )
      }
    }
  }

  def matchPresence(
      input: Message,
      pattern: Map[FieldDescriptor, Any],
      context: ExtensionResolutionContext
  ): Boolean = {
    pattern.forall { case (fd, v) =>
      if (!fd.isExtension()) {
        if (fd.getType() != Type.MESSAGE)
          if (fd.isRepeated) {
            input.getRepeatedFieldCount(fd) > 0
          } else {
            input.hasField(fd)
          }
        else {
          input.hasField(fd) && matchPresence(
            input.getField(fd).asInstanceOf[Message],
            v.asInstanceOf[Map[FieldDescriptor, Any]],
            context
          )
        }
      } else {
        input.getUnknownFields().hasField(fd.getNumber) &&
        matchPresence(
          getExtensionField(input, fd),
          v.asInstanceOf[Map[FieldDescriptor, Any]],
          context
        )
      }
    }
  }

  def processFieldTransformations(
      f: FileDescriptor,
      transforms: Seq[ResolvedFieldTransformation]
  ): Seq[AuxFieldOptions] =
    if (transforms.isEmpty) Seq.empty
    else {
      val extensions: Set[FieldDescriptor]  = fieldExtensionsForFile(f)
      def processFile: Seq[AuxFieldOptions] =
        f.getMessageTypes().asScala.flatMap(processMessage(_)).toSeq

      def processMessage(m: Descriptor): Seq[AuxFieldOptions] = {
        m.getFields().asScala.flatMap(processField(_)) ++
          m.getNestedTypes().asScala.flatMap(processMessage(_)).toSeq
      }.toSeq

      def processField(fd: FieldDescriptor): Seq[AuxFieldOptions] =
        if (transforms.nonEmpty) {
          val noReg   = FieldDescriptorProto.parseFrom(fd.toProto().toByteArray())
          val context = ExtensionResolutionContext(f.getFullName(), extensions)
          transforms.flatMap { transform =>
            if (matches(f.getFullName(), noReg, transform))
              Seq(
                AuxFieldOptions.newBuilder
                  .setTarget(fd.getFullName())
                  .setOptions(interpolateStrings(transform.set, fd.toProto(), context))
                  .build
              )
            else Seq.empty
          }
        } else Seq.empty
      processFile
    }

  def fieldExtensionsForFile(f: FileDescriptor): Set[FieldDescriptor] = {
    (f.getExtensions()
      .asScala
      .filter(
        // Comparing the descriptors references directly will not work. The google.protobuf.FieldOptions
        // we will get from `getContainingType` is the one we get from parsing the code generation request
        // inputs, which are disjoint from the compilerplugin's FieldOptions.
        _.getContainingType.getFullName == FieldOptions.getDescriptor().getFullName()
      ) ++ f.getDependencies().asScala.flatMap(fieldExtensionsForFile(_))).toSet
  }

  // Like m.getAllFields(), but also resolves unknown fields from extensions available in the scope
  // of the message.
  def fieldMap(
      m: Message,
      context: ExtensionResolutionContext
  ): Map[FieldDescriptor, Any] = {
    val unknownFields = for {
      number <- m.getUnknownFields().asMap().keySet().asScala
    } yield {
      val ext = context.extensions
        .find(_.getNumber == number)
        .getOrElse(
          throw new GeneratorException(
            s"${context.currentFile}: Could not find extension number $number when processing a field " +
              "transformation. A proto file defining this extension needs to be imported directly or transitively in this file."
          )
        )
      ext -> fieldMap(getExtensionField(m, ext), context)
    }

    val knownFields = m.getAllFields().asScala.map { case (field, value) =>
      if (field.getType() == Type.MESSAGE && !field.isOptional()) {
        throw new GeneratorException(
          s"${context.currentFile}: matching is supported only for scalar types and optional message fields."
        )
      }
      (field -> (if (field.getType() == Type.MESSAGE)
                   fieldMap(value.asInstanceOf[Message], context)
                 else value))
    }

    unknownFields.toMap ++ knownFields
  }

  def getExtensionField(
      m: Message,
      ext: FieldDescriptor
  ): Message = {
    if (ext.getType != Type.MESSAGE || !ext.isOptional) {
      throw new GeneratorException(
        s"Unknown extension fields must be optional message types: ${ext}"
      )
    }
    val fieldValue = m.getUnknownFields().getField(ext.getNumber())
    DynamicMessage.parseFrom(
      ext.getMessageType(),
      fieldValue.getLengthDelimitedList().asScala.foldLeft(ByteString.EMPTY)(_.concat(_))
    )
  }

  def splitPath(s: String): List[String] = splitPath0(s, s)

  private def splitPath0(s: String, allPath: String): List[String] = {
    if (s.isEmpty()) throw new GeneratorException(s"Got empty path component in $allPath")
    else {
      val tokenEnd = if (s.startsWith("[")) {
        val end = s.indexOf(']') + 1
        if (end == -1) throw new GeneratorException(s"Unmatched [ in path $allPath")
        if (s.length < end + 1 || s(end) != '.')
          throw new GeneratorException(
            s"Extension can not be the last path component. Expected . after ] in $allPath"
          )
        end
      } else {
        s.indexOf('.')
      }
      if (tokenEnd == -1) s :: Nil
      else s.substring(0, tokenEnd) :: splitPath0(s.substring(tokenEnd + 1), allPath)
    }
  }

  def fieldByPath(
      message: Message,
      path: String,
      context: ExtensionResolutionContext
  ): String =
    if (path.isEmpty()) throw new GeneratorException("Got an empty path")
    else
      fieldByPath(message, splitPath(path), path, context) match {
        case Left(error)  => throw new GeneratorException(error)
        case Right(value) => value
      }

  private[compiler] def fieldByPath(
      message: Message,
      path: List[String],
      allPath: String,
      context: ExtensionResolutionContext
  ): Either[String, String] = {
    for {
      fieldName <- path.headOption.toRight("Got an empty path")
      fd        <-
        if (fieldName.startsWith("["))
          context.extensions
            .find(_.getFullName == fieldName.substring(1, fieldName.length() - 1))
            .toRight(
              s"Could not find extension $fieldName when resolving $allPath"
            )
        else
          Option(message.getDescriptorForType().findFieldByName(fieldName))
            .toRight(
              s"Could not find field named $fieldName when resolving $allPath"
            )
      _ <-
        if (
          fd.isExtension() && fd
            .getContainingType()
            .getFullName != message.getDescriptorForType().getFullName()
        ) {
          val containingType = fd.getContainingType().getFullName()

          val dym =
            if (containingType == "google.protobuf.FieldOptions")
              s" Did you mean options.${fieldName} ?"
            else ""

          Left(s"Extension $fieldName is not an extension of ${message
              .getDescriptorForType()
              .getFullName()}, it is an extension of ${fd.getContainingType().getFullName()}.$dym")
        } else Right(())
      _ <-
        if (fd.isRepeated()) Left("Repeated fields are not supported")
        else Right(())
      v = if (fd.isExtension) getExtensionField(message, fd) else message.getField(fd)
      res <- path match {
        case _ :: Nil  => Right(v.toString())
        case _ :: tail =>
          if (fd.getType() == Type.MESSAGE)
            fieldByPath(v.asInstanceOf[Message], tail, allPath, context)
          else
            Left(
              s"Type ${fd.getType.toString} does not have a field ${tail.head} in $allPath"
            )
        case Nil => Left("Unexpected empty path")
      }
    } yield res
  }

  // Substitutes $(path) references in string fields within msg with values coming from the data
  // message
  private[compiler] def interpolateStrings[T <: Message](
      msg: T,
      data: Message,
      context: ExtensionResolutionContext
  ): T = {
    val b = msg.toBuilder()
    for {
      (field, value) <- msg.getAllFields().asScala
    } field.getType() match {
      case Type.STRING if (!field.isRepeated()) =>
        b.setField(field, interpolate(value.asInstanceOf[String], data, context))
      case Type.MESSAGE =>
        if (field.isRepeated())
          b.setField(
            field,
            value
              .asInstanceOf[java.util.List[Message]]
              .asScala
              .map(interpolateStrings(_, data, context))
              .asJava
          )
        else
          b.setField(
            field,
            interpolateStrings(value.asInstanceOf[Message], data, context)
          )
      case _ =>
    }
    b.build().asInstanceOf[T]
  }

  val FieldPath: java.util.regex.Pattern =
    raw"[$$]\(([a-zA-Z0-9_.\[\]]*)\)".r.pattern

  // Interpolates paths in the given string with values coming from the data message
  private[compiler] def interpolate(
      value: String,
      data: Message,
      context: ExtensionResolutionContext
  ): String =
    replaceAll(value, FieldPath, m => fieldByPath(data, m.group(1), context))

  // Matcher.replaceAll appeared on Java 9, so we have this Java 8 compatible version instead. Adapted
  // from https://stackoverflow.com/a/43372206/97524
  private[compiler] def replaceAll(
      templateText: String,
      pattern: Pattern,
      replacer: Matcher => String
  ): String = {
    val matcher = pattern.matcher(templateText)
    val result  = new StringBuffer()
    while (matcher.find()) {
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacer.apply(matcher)));
    }
    matcher.appendTail(result);
    result.toString()
  }
}
