package scalapb.compiler

import com.google.protobuf.Descriptors.Descriptor

case class SealedOneof(container: Descriptor, cases: Seq[Descriptor])

class SealedOneofsCache(values: Seq[SealedOneof]) {
  private val containerToCases: Map[Descriptor, Seq[Descriptor]] =
    values.map(s => s.container -> s.cases).toMap
  private val caseToContainer: Map[Descriptor, Descriptor] =
    values.flatMap(s => s.cases.map(_ -> s.container)).toMap
  def getCases(container: Descriptor): Option[Seq[Descriptor]] = containerToCases.get(container)
  def getContainer(acase: Descriptor): Option[Descriptor]      = caseToContainer.get(acase)
}
