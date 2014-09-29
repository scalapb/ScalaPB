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
    def add(words: String*) = copy(names = names ++ words)

    def nest(name: String) =
      Namespace(Set(), parent = Some(add(name)))

    def isNameAvailable(name: String): Boolean = !names.contains(name) && parent.forall(_.isNameAvailable(name))

    def generateName: Gen[String] = GenerateProtos.identifier.retryUntil(isNameAvailable)
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
      case (name, state) => (name, _nextFileId, copy(
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
    (names, state) <- GenUtils.listWithStatefulGen(state, minSize = 1)(_.generateName)
    values <- GenUtils.genListOfDistinctPositiveNumbers(names.size)
  } yield (EnumNode(myId, enumName, names zip values,
      parentMessageId = parentMessageId, fileId = state.currentFileId), state)

  def genMessageNode(depth: Int = 0, parentMessageId: Option[Int] = None)(state: State): Gen[(MessageNode, State)] =
    sized {
      s =>
        for {
          (myId, state) <- Gen.const(state.nextMessageId)
          (name, state) <- state.generateSubspace
          (messages, state) <- listWithStatefulGen(state, maxSize = (3 - depth) max 0)(genMessageNode(depth + 1, Some(myId)))
          (enums, state) <- listWithStatefulGen(state, maxSize = 3)(genEnumNode(Some(myId)))
          fieldCount <- choose[Int](0, s min 20)
          (fieldNames, state) <- listOfNWithStatefulGen(fieldCount, state)(_.generateName)
          fieldTags <- genListOfDistinctPositiveNumbers(fieldCount)
          fieldTypes <- listOfN(fieldCount, GenTypes.genFieldType(state)).map(_.toSeq)
          fieldOptions <- Gen.sequence[Seq, GenTypes.FieldOptions.Value](fieldTypes.map(GenTypes.genOptionsForField(myId, _)))
          fields = fieldNames zip ((fieldTypes, fieldOptions, fieldTags).zipped).toList map {
            case (n, (t, opts, tag)) => FieldNode(n, t, opts, tag)
          }
        } yield (MessageNode(myId, name, messages, enums, fields, parentMessageId,
          state.currentFileId), state.closeNamespace)
    }

  def genFileNode(state: State): Gen[(FileNode, State)] = sized {
    s =>
      for {
        (baseName, fileId, state) <- state.newFile
        (javaPackageNames, state) <- GenUtils.listWithStatefulGen(state, maxSize = 4)(_.generateName)
        javaPackage = javaPackageNames mkString "."
        javaPackageOption = if (javaPackage.nonEmpty) Some(javaPackage) else None
        (protoPackage, state) <- Gen.oneOf(state.generateSubspace, Gen.const(("", state)))
        protoPackageOption = if (protoPackage.nonEmpty) Some(protoPackage) else None
        (messages, state) <- listWithStatefulGen(state, maxSize = 4)(genMessageNode(0, None))
        (enums, state) <- listWithStatefulGen(state, maxSize = 5)(genEnumNode(None))
      } yield (FileNode(baseName, protoPackageOption, javaPackageOption, messages, enums, fileId),
        if (protoPackage.isEmpty) state else state.closeNamespace)
  }

  def genRootNode: Gen[RootNode] =
    listWithStatefulGen(State(), maxSize = 12)(genFileNode).map {
      case (files, state) => assert(state.namespace.parent.isEmpty); RootNode(files)
    }
}
