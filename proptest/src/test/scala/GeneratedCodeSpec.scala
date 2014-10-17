import Nodes.RootNode
import SchemaGenerators.CompiledSchema
import com.google.protobuf
import com.google.protobuf.TextFormat
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import org.scalacheck.Arbitrary
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class GeneratedCodeSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  implicit def arbitraryGraph: Arbitrary[RootNode] = Arbitrary(GraphGen.genRootNode)

  implicit def arbitraryCompiledSchema: Arbitrary[CompiledSchema] = Arbitrary(SchemaGenerators.genCompiledSchema)

  property("min and max id are consecutive over files") {
    forAll(GraphGen.genRootNode) {
      node =>
        def validateMinMax(pairs: Seq[(Int, Int)]) =
          pairs.sliding(2).filter(_.size == 2).forall {
            case Seq((min1, max1), (min2, max2)) => min2 == max1 + 1 && min2 <= max2
          }
        val messageIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minMessageId.map((_, f.maxMessageId.get)))}
        val enumIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minEnumId.map((_, f.maxEnumId.get)))}
        validateMinMax(messageIdPairs) && validateMinMax(enumIdPairs)
    }
  }

  property("Java and Scala protos are equivalent") {
    forAll(SchemaGenerators.genCompiledSchema, workers(1), minSuccessful(20)) {
      schema: CompiledSchema =>
        forAll(GenData.genMessageValueInstance(schema.rootNode)) {
          case (message, messageValue) =>
            // Ascii to binary is the same.
            val messageAscii = messageValue.toAscii
            val builder = schema.javaBuilder(message)
            TextFormat.merge(messageAscii, builder)
            val javaProto: protobuf.Message = builder.build()
            val companion: GeneratedMessageCompanion[_ <: GeneratedMessage] = schema.scalaObject(message)
            val scalaProto = companion.fromAscii(messageValue.toAscii)
            val scalaBytes = scalaProto.toByteArray

            // Serialized values should be the same
            scalaBytes should be(javaProto.toByteArray)

            // Parsing the serialized bytes should give the same object.
            val scalaParsedFromBytes = companion.parseFrom(scalaBytes)
            scalaParsedFromBytes.toString should be(scalaProto.toString)
            scalaParsedFromBytes should be(scalaProto)
        }
    }
  }
}
