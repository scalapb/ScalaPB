package scalapb.compiler

import com.google.protobuf.Message
import scalapb.options.Scalapb.FieldTransformation.MatchType
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FileDescriptor
import scala.jdk.CollectionConverters._
import com.google.protobuf.DescriptorProtos.FieldOptions
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
    matchType: MatchType
)

private[compiler] object ResolvedFieldTransformation {
  def apply(
      currentFile: String,
      ft: FieldTransformation,
      extensions: Seq[FieldDescriptor]
  ): ResolvedFieldTransformation = {
    ResolvedFieldTransformation(
      FieldTransformations.fieldMap(
        currentFile,
        FieldOptions.parseFrom(ft.getWhen.toByteArray()),
        extensions = extensions
      ),
      ft.getSet(),
      ft.getMatchType()
    )
  }
}

private[compiler] object FieldTransformations {
  def matches[T <: Message](
      currentFile: String,
      input: Map[FieldDescriptor, Any],
      transformation: ResolvedFieldTransformation
  ): Boolean = {
    transformation.matchType match {
      case MatchType.CONTAINS =>
        matchContains(currentFile, input, transformation.whenFields)
      case MatchType.EXACT    => input == transformation.whenFields
      case MatchType.PRESENCE => matchPresence(input, transformation.whenFields)
    }
  }

  def matchContains(
      currentFile: String,
      input: Map[FieldDescriptor, Any],
      pattern: Map[FieldDescriptor, Any]
  ): Boolean = {
    pattern.forall {
      case (fd, v) =>
        input.get(fd) match {
          case None => false
          case Some(u) =>
            if (fd.getType() != Type.MESSAGE)
              u == v
            else
              matchContains(
                currentFile,
                fieldMap(currentFile, u.asInstanceOf[Message], Seq.empty),
                fieldMap(currentFile, v.asInstanceOf[Message], Seq.empty)
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
      val extensions: Seq[FieldDescriptor] = fieldExtensionsForFile(f)
      def processFile: Seq[AuxFieldOptions] =
        f.getMessageTypes().asScala.flatMap(processMessage(_)).toSeq

      def processMessage(m: Descriptor): Seq[AuxFieldOptions] = {
        m.getFields().asScala.flatMap(processField(_)) ++
          m.getNestedTypes().asScala.flatMap(processMessage(_)).toSeq
      }.toSeq

      def processField(fd: FieldDescriptor): Seq[AuxFieldOptions] =
        if (transforms.nonEmpty) {
          val noReg = FieldOptions.parseFrom(fd.getOptions().toByteArray())
          val input = fieldMap(f.getFullName(), noReg, extensions)
          transforms.flatMap { transform =>
            if (matches(f.getFullName(), input, transform))
              Seq(
                AuxFieldOptions.newBuilder
                  .setTarget(fd.getFullName())
                  .setOptions(interpolateStrings(transform.set, fd.getOptions(), extensions))
                  .build
              )
            else Seq.empty
          }
        } else Seq.empty
      processFile
    }

  def matchPresence(
      input: Map[FieldDescriptor, Any],
      pattern: Map[FieldDescriptor, Any]
  ): Boolean = {
    pattern.forall {
      case (fd, value) =>
        if (fd.isRepeated())
          throw new GeneratorException(
            "Presence matching on repeated fields is not supported"
          )
        else if (fd.getType() == Type.MESSAGE && input.contains(fd))
          matchPresence(
            input(fd).asInstanceOf[Message].getAllFields().asScala.toMap,
            value.asInstanceOf[Message].getAllFields().asScala.toMap
          )
        else
          input.contains(fd)
    }
  }

  def fieldExtensionsForFile(f: FileDescriptor): Seq[FieldDescriptor] = {
    (f +: f.getDependencies().asScala.toSeq).flatMap {
      _.getExtensions().asScala.filter(
        // Comparing the descriptors references directly will not work. The google.protobuf.FieldOptions
        // we will get from `getContainingType` is the one we get from parsing the code generation request
        // inputs, which are disjoint from the compilerplugin's FieldOptions.
        _.getContainingType.getFullName == FieldOptions.getDescriptor().getFullName()
      )
    }
  }

  // Like m.getAllFields(), but also resolves unknown fields from extensions available in the scope
  // of the message.
  def fieldMap(
      currentFile: String,
      m: Message,
      extensions: Seq[FieldDescriptor]
  ): Map[FieldDescriptor, Any] = {
    val unknownFields = for {
      number <- m.getUnknownFields().asMap().keySet().asScala
    } yield {
      val ext = extensions
        .find(_.getNumber == number)
        .getOrElse(
          throw new GeneratorException(
            s"$currentFile: Could not find extension number $number for message ${m.toString()}"
          )
        )
      ext -> getExtensionField(m, ext)
    }

    unknownFields.toMap ++ m.getAllFields().asScala
  }

  def getExtensionField(m: Message, ext: FieldDescriptor): Message = {
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
      extensions: Seq[FieldDescriptor]
  ): String =
    if (path.isEmpty()) throw new GeneratorException("Got an empty path")
    else
      fieldByPath(message, splitPath(path), path, extensions) match {
        case Left(error)  => throw new GeneratorException(error)
        case Right(value) => value
      }

  private[compiler] def fieldByPath(
      message: Message,
      path: List[String],
      allPath: String,
      extensions: Seq[FieldDescriptor]
  ): Either[String, String] = {
    for {
      fieldName <- path.headOption.toRight("Got an empty path")
      fd <- if (fieldName.startsWith("["))
        extensions
          .find(_.getFullName == fieldName.substring(1, fieldName.length() - 1))
          .toRight(
            s"Could not find extension $fieldName when resolving $allPath"
          )
      else
        Option(message.getDescriptorForType().findFieldByName(fieldName))
          .toRight(
            s"Could not find field named $fieldName when resolving $allPath"
          )
      _ <- if (fd.isRepeated()) Left("Repeated fields are not supported")
      else Right(())
      v = if (fd.isExtension) getExtensionField(message, fd) else message.getField(fd)
      res <- path match {
        case _ :: Nil => Right(v.toString())
        case _ :: tail =>
          if (fd.getType() == Type.MESSAGE)
            fieldByPath(v.asInstanceOf[Message], tail, allPath, extensions)
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
      extensions: Seq[FieldDescriptor]
  ): T = {
    val b = msg.toBuilder()
    for {
      (field, value) <- msg.getAllFields().asScala
    } field.getType() match {
      case Type.STRING if (!field.isRepeated()) =>
        b.setField(field, interpolate(value.asInstanceOf[String], data, extensions))
      case Type.MESSAGE =>
        if (field.isRepeated())
          b.setField(
            field,
            value
              .asInstanceOf[java.util.List[Message]]
              .asScala
              .map(interpolateStrings(_, data, extensions))
              .asJava
          )
        else
          b.setField(
            field,
            interpolateStrings(value.asInstanceOf[Message], data, extensions)
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
      extensions: Seq[FieldDescriptor]
  ): String =
    replaceAll(value, FieldPath, m => fieldByPath(data, m.group(1), extensions))

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
      matcher.appendReplacement(result, replacer.apply(matcher));
    }
    matcher.appendTail(result);
    result.toString()
  }
}
