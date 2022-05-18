package scalapb.changed

import com.google.protobuf.ByteString
import scalapb.TypeMapper

package object scoped {
  implicit val bs2ba: TypeMapper[ByteString, Array[Byte]] = TypeMapper({ (bs: ByteString) =>
    bs.toByteArray()
  })(
    { (ba: Array[Byte]) => ByteString.copyFrom(ba) }
  )

}
