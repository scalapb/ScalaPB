import GenUtils._
import org.scalacheck.Gen

object GraphGen {
  import Nodes._
  import org.scalacheck.Gen._

  trait StatefulGenerator[S] {
    def nextMessageId: (S, StatefulGenerator[S])
  }

  case class Namespace(names: Set[String], parent: Option[Namespace]) {
    // Returns a new namespace nested in this one.
    // Adds the given name to the namespace.
    def add(words: String*) = copy(names = names ++ words.map(_.toLowerCase))

    def nest(name: String) =
      Namespace(Set(), parent = Some(add(name)))

    def isNameAvailable(name: String): Boolean = !names.contains(name.toLowerCase) && parent.forall(_.isNameAvailable(name))

    def generateName: Gen[String] = SchemaGenerators.identifier.retryUntil(isNameAvailable)
  }

  val ROOT_NAMESPACE = Namespace(Set("foo", "bar"), None)

  case class State(_nextMessageId: Int = 0,
                   _nextEnumId: Int = 0,
                   _nextFileId: Int = 0,
                   currentFileInitialMessageId: Int = 0,
                   currentFileInitialEnumId: Int = 0,
                   namespace: Namespace = ROOT_NAMESPACE) extends StatefulGenerator[Int] {
    def nextMessageId = (_nextMessageId, copy(_nextMessageId = _nextMessageId + 1))

    def nextEnumId = (_nextEnumId, copy(_nextEnumId = _nextEnumId + 1))

    def newFile: Gen[(String, Int, State)] = generateName.map {
      case (name, state) => (name, _nextFileId, state.copy(
        currentFileInitialMessageId = _nextMessageId,
        currentFileInitialEnumId = _nextEnumId,
        _nextFileId = _nextFileId + 1))
    }

    def currentFileId: Int = _nextFileId - 1

    def generateName: Gen[(String, State)] = namespace.generateName.map {
      s => (s, this.copy(namespace = namespace.add(s)))
    }

    def generateSubspace: Gen[(String, State)] = namespace.generateName.map {
      s => (s, this.copy(namespace = namespace.nest(s)))
    }

    def closeNamespace: State = namespace.parent.fold(
      throw new IllegalStateException("Attempt to close root namespace"))(p => copy(namespace = p))
  }

  def genEnumNode(parentMessageId: Option[Int])(state: State): Gen[(EnumNode, State)] = for {
    (enumName, state) <- state.generateName
    (myId, state) <- Gen.const(state.nextEnumId)
    (names, state) <- GenUtils.listWithStatefulGen(state, minSize = 1, maxSize = 5)(_.generateName)
    values <- GenUtils.genListOfDistinctPositiveNumbers(names.size)
  } yield (EnumNode(myId, enumName, names zip values,
      parentMessageId = parentMessageId, fileId = state.currentFileId), state)

  sealed trait OneOfGrouping {
    def isOneof: Boolean
    def name: String
  }
  case object NotInOneof extends OneOfGrouping {
    def isOneof = false
    def name: String = throw new RuntimeException
  }
  case class OneofContainer(name: String) extends OneOfGrouping {
    def isOneof = true
  }

  def genOneOfs(fieldCount: Int, state: State): Gen[(List[OneOfGrouping], State)] = {
    def genBits(n: Int, seqSize: Int, prev: OneOfGrouping, state: State): Gen[(List[OneOfGrouping], State)] =
      if (n == 0) (Gen.const(Nil, state))
      else Gen.frequency(
        (4, genBits(n - 1, 0, NotInOneof, state).map {
          case (l, s) => (NotInOneof :: l, s) }),
        (1, for {
          (name, state) <- state.generateName
          (tail, state) <- genBits(n - 1, 1, OneofContainer(name), state)
        } yield (OneofContainer(name) :: tail, state)),
        (if (seqSize > 0) 4 else 0, genBits(n - 1, 1, prev, state).map {
          case (l, s) => (prev :: l, s) }))

    genBits(fieldCount, 0, NotInOneof, state)
  }

  def genMessageNode(depth: Int = 0, parentMessageId: Option[Int] = None)(state: State): Gen[(MessageNode, State)] =
    sized {
      s =>
        for {
          (myId, state) <- Gen.const(state.nextMessageId)
          (name, state) <- state.generateSubspace
          (messages, state) <- listWithStatefulGen(state, maxSize = (3 - depth) max 0)(genMessageNode(depth + 1, Some(myId)))
          (enums, state) <- listWithStatefulGen(state, maxSize = 3)(genEnumNode(Some(myId)))
          fieldCount <- choose[Int](0, s min 15)
          (fieldNames, state) <- listOfNWithStatefulGen(fieldCount, state)(_.generateName)
          fieldTags <- genListOfDistinctPositiveNumbers(fieldCount)
          fieldTypes <- listOfN(fieldCount, GenTypes.genFieldType(state)).map(_.toSeq)
          (oneOfGroupings, state) <- genOneOfs(fieldCount, state)
          fieldOptions <- Gen.sequence[Seq, GenTypes.FieldOptions](fieldTypes.map(GenTypes.genOptionsForField(myId, _)))
          fields = (fieldNames zip oneOfGroupings) zip ((fieldTypes, fieldOptions, fieldTags).zipped).toList map {
            case ((n, oog), (t, opts, tag)) => FieldNode(n, t, opts, oog, tag)
          }
        } yield (MessageNode(myId, name, messages, enums, fields, parentMessageId,
          state.currentFileId), state.closeNamespace)
    }

  def genFileNode(state: State): Gen[(FileNode, State)] = sized {
    s =>
      for {
        (baseName, fileId, state) <- state.newFile
        (javaPackageNames, state) <- GenUtils.listWithStatefulGen(state, minSize = 1, maxSize = 4)(_.generateName)
        javaPackage = javaPackageNames mkString "."
        javaPackageOption = if (javaPackage.nonEmpty) Some(javaPackage) else None
        (protoPackage, state) <- Gen.oneOf(state.generateSubspace, Gen.const(("", state)))
        protoPackageOption = if (protoPackage.nonEmpty) Some(protoPackage) else None
        (messages, state) <- listWithStatefulGen(state, maxSize = 4)(genMessageNode(0, None))
        (enums, state) <- listWithStatefulGen(state, maxSize = 3)(genEnumNode(None))
      } yield (FileNode(baseName, protoPackageOption, javaPackageOption, messages, enums, fileId),
        if (protoPackage.isEmpty) state else state.closeNamespace)
  }

  def genRootNode: Gen[RootNode] =
    listWithStatefulGen(State(), maxSize = 10)(genFileNode).map {
      case (files, state) =>
        assert(state.namespace.parent.isEmpty)
        RootNode(files)
    }.suchThat(_.maxMessageId.isDefined)
}
