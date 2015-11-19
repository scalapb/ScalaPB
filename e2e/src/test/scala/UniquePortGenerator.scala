import java.net.ServerSocket

import scala.util.Random

object UniquePortGenerator {
  private[this] val usingPorts = collection.mutable.HashSet.empty[Int]

  def getOpt(): Option[Int] = synchronized {
    @annotation.tailrec
    def loop(loopCount: Int): Option[Int] = {
      val socket = new ServerSocket(0)
      val port = try {
        socket.getLocalPort
      } finally {
        socket.close()
      }

      if (usingPorts(port)) {
        if (loopCount == 0) {
          None
        } else {
          Thread.sleep(Random.nextInt(50))
          loop(loopCount - 1)
        }
      } else {
        usingPorts += port
        Option(port)
      }
    }
    loop(30)
  }

  def get(): Int = getOpt().getOrElse(sys.error("could not get port"))
}
