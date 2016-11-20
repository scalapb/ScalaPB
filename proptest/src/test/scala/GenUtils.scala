import org.scalacheck.Gen

/**
 * Created by thesamet on 9/28/14.
 */
object GenUtils {

  import org.scalacheck.Gen._

  def listOfNWithStatefulGen[A, State](n: Int, state: State)(
    f: State => Gen[(A, State)]): Gen[(Seq[A], State)] =
    if (n <= 0) (List(), state)
    else for {
      (acc, state1) <- listOfNWithStatefulGen(n - 1, state)(f)
      (last, state2) <- f(state1)
    } yield (acc :+ last, state2)

  def listWithStatefulGen[A, State](state: State, minSize: Int = 0, maxSize: Int = Integer.MAX_VALUE)(f: State => Gen[(A, State)]): Gen[(Seq[A], State)] = sized {
    s =>
      for {
        size <- choose[Int](minSize, s min maxSize)
        (l, idState) <- listOfNWithStatefulGen(size, state)(f)
      } yield (l, idState)
  }

  def genListOfDistinctPositiveNumbers(size: Int) = Gen.parameterized {
    params =>
      Gen.listOfN(size, Gen.chooseNum(1, 10)).map(_.scan(0)(_ + _).tail).map(scala.util.Random.shuffle(_))
  }
}
