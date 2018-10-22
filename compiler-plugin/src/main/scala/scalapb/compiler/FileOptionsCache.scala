package scalapb.compiler
import com.google.protobuf.Descriptors.FileDescriptor
import scalapb.options.compiler.Scalapb
import scalapb.options.compiler.Scalapb.ScalaPbOptions
import scalapb.options.compiler.Scalapb.ScalaPbOptions.OptionsScope

import scala.collection.mutable

case class PackageScopedOptions(fileName: String, options: ScalaPbOptions)

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

  def buildCache(files: Seq[FileDescriptor]): Map[FileDescriptor, ScalaPbOptions] = {
    val filesWithPackageScope =
      files.filter(_.getOptions.getExtension(Scalapb.options).getScope == OptionsScope.PACKAGE)

    val filesAndOptions: Seq[(String, PackageScopedOptions)] =
      filesWithPackageScope
        .map { file =>
          file.getPackage -> PackageScopedOptions(
            file.getName,
            file.getOptions.getExtension(Scalapb.options)
          )
        }
        .sortBy(_._1.length) // so parent packages come before subpackages

    filesAndOptions.filter(_._1.isEmpty).foreach {
      case (pn, pso) =>
        throw new GeneratorException(
          s"${pso.fileName}: a package statement is required when package-scoped options are used"
        )
    }

    filesAndOptions.groupBy(_._1).find(_._2.length > 1).foreach {
      case (pn, s) =>
        throw new GeneratorException(
          s"Multiple files contain package-scoped options for package '${pn}': ${s.map(_._2.fileName).sorted.mkString(", ")}"
        )
    }

    val optionsByPackage = new mutable.HashMap[String, ScalaPbOptions]

    filesAndOptions.foreach {
      case (packageName, packageScopedOptions) =>
        val parents = parentPackages(packageName)
        val actualOptions = parents.find(optionsByPackage.contains) match {
          case Some(p) => mergeOptions(optionsByPackage(p), packageScopedOptions.options)
          case None    => packageScopedOptions.options
        }
        optionsByPackage += packageName -> actualOptions
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
      f -> opts
    }.toMap
  }
}
