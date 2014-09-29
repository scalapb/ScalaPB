import org.scalacheck.Gen

/**
 * Created by thesamet on 9/28/14.
 */
object GenUtils {

  import org.scalacheck.Gen._

  def listOfNWithStatefulGen[A, B](n: Int, gen: B)(
    f: B => Gen[(A, B)]): Gen[(Seq[A], B)] =
    if (n <= 0) (List(), gen)
    else for {
      (acc, gen1) <- listOfNWithStatefulGen(n - 1, gen)(f)
      (last, gen2) <- f(gen1)
    } yield (acc :+ last, gen2)

  def listWithStatefulGen[A, B](gen: B, minSize: Int = 0, maxSize: Int = Integer.MAX_VALUE)(f: B => Gen[(A, B)]): Gen[(Seq[A], B)] = sized {
    s =>
      for {
        size <- choose[Int](minSize, s min maxSize)
        (l, idGen) <- listOfNWithStatefulGen(size, gen)(f)
      } yield (l, idGen)
  }

  def genListOfDistinctPositiveNumbers(size: Int) = Gen.parameterized {
    params =>
      Gen.listOfN(size, Gen.chooseNum(1, 10)).map(_.scan(0)(_ + _).tail).map(params.rng.shuffle[Int, Seq])
  }
}
