package scalapb.compiler
import com.google.protobuf.Descriptors.FileDescriptor
import scalapb.options.Scalapb
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.options.Scalapb.ScalaPbOptions.OptionsScope

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Success
import scala.util.Failure
import scalapb.options.Scalapb.PreprocesserOutput

private[this] case class PackageScopedOptions(
    fileName: String,
    `package`: String,
    options: ScalaPbOptions
)

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
    ScalaPbOptions
      .newBuilder(parent)
      .mergeFrom(child)
      .setScope(child.getScope) // retain child's scope
      .build()
  }

  @deprecated(
    "Use buildCache that takes SecondaryOutputProvider. Preprocessors will not work",
    "0.10.10"
  )
  def buildCache(
      files: Seq[FileDescriptor]
  ): Map[FileDescriptor, ScalaPbOptions] = buildCache(files, SecondaryOutputProvider.empty)

  def buildCache(
      files: Seq[FileDescriptor],
      secondaryOutputProvider: SecondaryOutputProvider
  ): Map[FileDescriptor, ScalaPbOptions] = {

    val givenPackageOptions: Seq[PackageScopedOptions] =
      files
        .filter(_.getOptions.getExtension(Scalapb.options).getScope == OptionsScope.PACKAGE)
        .map { file =>
          PackageScopedOptions(
            file.getName,
            file.getPackage,
            file.getOptions.getExtension(Scalapb.options)
          )
        }
        .sortBy(_.`package`.length) // so parent packages come before subpackages

    givenPackageOptions.filter(_.`package`.isEmpty).foreach { pso =>
      throw new GeneratorException(
        s"${pso.fileName}: a package statement is required when package-scoped options are used"
      )
    }

    givenPackageOptions.groupBy(_.`package`).find(_._2.length > 1).foreach { case (pn, s) =>
      throw new GeneratorException(
        s"Multiple files contain package-scoped options for package '${pn}': ${s.map(_.fileName).sorted.mkString(", ")}"
      )
    }

    givenPackageOptions.find(_.options.hasObjectName).foreach { pso =>
      throw new GeneratorException(
        s"${pso.fileName}: object_name is not allowed in package-scoped options."
      )
    }

    // Merge package-scoped options of parent packages with sub-packages, so for each package it
    // is sufficient to look up the nearest parent package that has package-scoped options.
    val optionsByPackage = new mutable.HashMap[String, ScalaPbOptions]

    givenPackageOptions.foreach { case pso =>
      val parents: List[String] = parentPackages(pso.`package`)
      val actualOptions = parents.find(optionsByPackage.contains) match {
        case Some(p) => mergeOptions(optionsByPackage(p), pso.options)
        case None    => pso.options
      }
      optionsByPackage += pso.`package` -> actualOptions
    }

    files.map { f =>
      val opts = if (f.getOptions.getExtension(Scalapb.options).getScope == OptionsScope.PACKAGE) {
        optionsByPackage(f.getPackage)
      } else {
        (f.getPackage :: parentPackages(f.getPackage)).find(optionsByPackage.contains) match {
          case Some(p) =>
            mergeOptions(optionsByPackage(p), f.getOptions.getExtension(Scalapb.options))
          case None => f.getOptions.getExtension(Scalapb.options)
        }
      }

      val preprocessors = opts.getPreprocessorsList().asScala.flatMap { name =>
        if (!SecondaryOutputProvider.isNameValid(name))
          throw new GeneratorException(s"${f.getFullName()}: Invalid preprocessor name: '$name'")
        secondaryOutputProvider.get(name) match {
          case Success(output) =>
            FileOptionsCache.validatePreprocessorOutput(name, output)
            Some(output)
          case Failure(exception) =>
            throw GeneratorException(s"${f.getFullName}: ${exception.getMessage()}")
        }
      }

      val preprocessorsProvidedOptions = for {
        proc <- preprocessors
        fn   <- Option(proc.getOptionsByFileMap.get(f.getFullName()))
      } yield fn

      val effectiveOpts = preprocessorsProvidedOptions.foldRight(opts)(mergeOptions(_, _))

      f -> effectiveOpts
    }.toMap
  }

  private[scalapb] def validatePreprocessorOutput(
      name: String,
      output: PreprocesserOutput
  ): PreprocesserOutput = {
    output.getOptionsByFileMap().asScala.find(_._2.getScope() != OptionsScope.FILE).foreach { ev =>
      throw new GeneratorException(
        s"Preprocessor options must be file-scoped. Preprocessor '${name}' provided scope '${ev._2
          .getScope()}' for file ${ev._1}."
      )
    }
    output
  }
}
