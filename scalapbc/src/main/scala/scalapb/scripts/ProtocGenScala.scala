package scalapb.scripts
import com.google.protobuf.CodedInputStream
import scalapb.ScalaPbCodeGenerator

object ProtocGenScala {
  def main(args: Array[String]): Unit = {
    System.out.write(ScalaPbCodeGenerator.run(CodedInputStream.newInstance(System.in)))
  }
}
