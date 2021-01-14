package scalapb.compiler
import com.google.protobuf.Descriptors.FileDescriptor
import scalapb.options.Scalapb
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.options.Scalapb.ScalaPbOptions.OptionsScope

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Success
import scala.util.Failure
import scalapb.options.Scalapb.PreprocessorOutput

object FileOptionsCache {
  def parentPackages(packageName: String): List[String] = {
    packageName
      .split('.')
      .scanLeft(Seq[String]())(_ :+ _)
      .drop(1)
      .dropRight(1)
      .map(_.mkString("."))
      .reverse
      .toList
  }

  def mergeOptions(parent: ScalaPbOptions, child: ScalaPbOptions) = {
    val r = ScalaPbOptions
      .newBuilder(parent)
      .mergeFrom(child)
      .setScope(child.getScope) // retain child's scope
    r.build()
  }

  def reducePackageOptions[T](
      files: Seq[(FileDescriptor, ScalaPbOptions)],
      data: (FileDescriptor, ScalaPbOptions) => T
  )(op: (T, T) => T): Map[FileDescriptor, T] = {
    val byPackage = new mutable.HashMap[String, T]
    val output    = new mutable.HashMap[FileDescriptor, T]

    // Process files by package name, and process package scoped options for each package first.
    files
      .sortBy(f => (f._1.getPackage(), f._2.getScope != OptionsScope.PACKAGE))
      .foreach {
        case (f, opts) =>
          val isPackageScoped = (opts.getScope == OptionsScope.PACKAGE)
          if (isPackageScoped && f.getPackage().isEmpty())
            throw new GeneratorException(
              s"${f.getFullName()}: a package statement is required when package-scoped options are used"
            )
          if (isPackageScoped && byPackage.contains(f.getPackage())) {
            val dups = files
              .filter(other =>
                other._1.getPackage() == f.getPackage() && other._2
                  .getScope() == OptionsScope.PACKAGE
              )
              .map(_._1.getFullName())
              .mkString(", ")
            throw new GeneratorException(
              s"Multiple files contain package-scoped options for package '${f.getPackage}': ${dups}"
            )
          }

          if (isPackageScoped && opts.hasObjectName())
            throw new GeneratorException(
              s"${f.getFullName()}: object_name is not allowed in package-scoped options."
            )

          val packagesToInheritFrom =
            if (isPackageScoped) parentPackages(f.getPackage())
            else f.getPackage() :: parentPackages(f.getPackage())

          val inherited = packagesToInheritFrom.find(byPackage.contains(_)).map(byPackage(_))

          val res = inherited match {
            case Some(base) => op(base, data(f, opts))
            case None       => data(f, opts)
          }
          output += f -> res
          if (isPackageScoped) {
            byPackage += f.getPackage -> res
          }
      }
    output.toMap
  }

  // For each file, which preprocessors are enabled
  def preprocessorsForFile(files: Seq[FileDescriptor]): Map[FileDescriptor, Seq[String]] =
    reducePackageOptions[Seq[String]](
      files.map(f => (f, f.getOptions.getExtension(Scalapb.options))),
      (_, opts) => opts.getPreprocessorsList.asScala.toSeq
    )((parent, child) => clearNegatedPreprocessors(parent ++ child))

  @deprecated(
    "Use buildCache that takes SecondaryOutputProvider. Preprocessors will not work",
    "0.10.10"
  )
  def buildCache(
      files: Seq[FileDescriptor]
  ): Map[FileDescriptor, ScalaPbOptions] = buildCache(files, SecondaryOutputProvider.empty)

  // Given a list of preprocessors, if it contains an opted-out preprocessor (in the form of -$name),
  // then it removes it from the list.
  private def clearNegatedPreprocessors(input: Seq[String]): Seq[String] = {
    val excludedPreprocessors = input.filter(_.startsWith("-")).map(_.tail)
    input.filter(p => !excludedPreprocessors.contains(p) && !p.startsWith("-"))
  }

  def buildCache(
      filesIn: Seq[FileDescriptor],
      secondaryOutputProvider: SecondaryOutputProvider
  ): Map[FileDescriptor, ScalaPbOptions] = {
    val preprocessorsByFile: Map[FileDescriptor, Seq[String]] = preprocessorsForFile(filesIn)

    for {
      (file, names) <- preprocessorsByFile
      name          <- names
    } {
      if (!SecondaryOutputProvider.isNameValid(name))
        throw new GeneratorException(
          s"${file.getFullName()}: Invalid preprocessor name: '$name'"
        )
    }

    val allPreprocessorNames: Set[String] = preprocessorsByFile.values.flatten.toSet
    val preprocessorValues: Map[String, PreprocessorOutput] = allPreprocessorNames.map { name =>
      secondaryOutputProvider.get(name) match {
        case Success(output) =>
          FileOptionsCache.validatePreprocessorOutput(name, output)
          name -> output
        case Failure(exception) =>
          val files = preprocessorsByFile
            .collect {
              case (fd, preprocessors) if preprocessors.contains(name) => fd.getFullName()
            }
            .mkString(", ")
          throw GeneratorException(s"$files: ${exception.getMessage()}")
      }
    }.toMap

    val processedOptions: Map[FileDescriptor, ScalaPbOptions] = preprocessorsByFile.map {
      case (file, preprocessorsForFile) =>
        file -> preprocessorsForFile
          .flatMap(name =>
            Option(preprocessorValues(name).getOptionsByFileMap.get(file.getFullName()))
          )
          .foldRight(file.getOptions().getExtension(Scalapb.options))(mergeOptions(_, _))
    }.toMap

    val fileOptions = reducePackageOptions[ScalaPbOptions](
      filesIn.map(f => (f, processedOptions(f))),
      (_, opts) => opts
    )(
      mergeOptions(_, _)
    )

    val fieldTransformations = reducePackageOptions[Seq[ResolvedFieldTransformation]](
      filesIn.map(f => (f, processedOptions(f))),
      (file, opts) =>
        opts
          .getFieldTransformationsList()
          .asScala
          .map(t =>
            ResolvedFieldTransformation(
              file.getFullName(),
              t,
              FieldTransformations.fieldExtensionsForFile(file)
            )
          )
          .toSeq
    )(_ ++ _)

    fileOptions.map {
      case (f, opts) =>
        f ->
          (if (opts.getIgnoreFieldTransformations) opts
           else
             opts.toBuilder
               .addAllAuxFieldOptions(
                 FieldTransformations
                   .processFieldTransformations(f, fieldTransformations(f))
                   .asJava
               )
               .build())
    }.toMap
  }

  private[scalapb] def validatePreprocessorOutput(
      name: String,
      output: PreprocessorOutput
  ): PreprocessorOutput = {
    output.getOptionsByFileMap().asScala.find(_._2.getScope() != OptionsScope.FILE).foreach { ev =>
      throw new GeneratorException(
        s"Preprocessor options must be file-scoped. Preprocessor '${name}' provided scope '${ev._2
          .getScope()}' for file ${ev._1}."
      )
    }
    output
  }
}
