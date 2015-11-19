import GenUtils._
import GenTypes.{FieldOptions, ProtoType, FieldModifier}
import com.trueaccord.scalapb.Scalapb.ScalaPbOptions
import com.trueaccord.scalapb.compiler.StreamType
import org.scalacheck.{Arbitrary, Gen}

object GraphGen {
  import Nodes._
  import org.scalacheck.Gen._

  trait StatefulGenerator[S] {
    def nextMessageId: (S, StatefulGenerator[S])
  }

  case class Namespace(names: Set[String], parent: Option[Namespace]) {
    // Adds the given name to the namespace.
    def add(words: String*) = copy(names = names ++ words.map(_.toLowerCase))

    // Returns a new namespace nested in this one.
    def nest(name: String) =
      Namespace(Set(), parent = Some(add(name)))

    def isNameAvailable(name: String): Boolean = !names.contains(name.toLowerCase) && parent.forall(_.isNameAvailable(name))

    def generateName: Gen[String] = SchemaGenerators.identifier.retryUntil(isNameAvailable)
  }

  val ROOT_NAMESPACE = Namespace(Set("foo", "bar"), None)

  case class State(_nextMessageId: Int = 0,
                   _nextEnumId: Int = 0,
                   _nextFileId: Int = 0,
                   proto3EnumIds: Vector[Int] = Vector.empty,
                   currentFileInitialMessageId: Int = 0,
                   currentFileInitialEnumId: Int = 0,
                   namespace: Namespace = ROOT_NAMESPACE) extends StatefulGenerator[Int] {
    def nextMessageId = (_nextMessageId, copy(_nextMessageId = _nextMessageId + 1))

    def nextEnumId(syntax: ProtoSyntax) = (_nextEnumId, copy(_nextEnumId = _nextEnumId + 1,
      proto3EnumIds = if (syntax.isProto3) proto3EnumIds :+ _nextEnumId else proto3EnumIds)
    )

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

  def genEnumNode(parentMessageId: Option[Int], syntax: ProtoSyntax)(state: State): Gen[(EnumNode, State)] = for {
    (enumName, state) <- state.generateName
    (myId, state) <- Gen.const(state.nextEnumId(syntax))
    (names, state) <- GenUtils.listWithStatefulGen(state, minSize = 1, maxSize = 5)(_.generateName)
    values <- GenUtils.genListOfDistinctPositiveNumbers(names.size).map {
      v =>
        // in proto3 the first enum value must be zero.
        if (syntax.isProto3) v.updated(0, 0) else v
    }
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

  def genMessageNode(depth: Int = 0, parentMessageId: Option[Int] = None, protoSyntax: ProtoSyntax)(state: State): Gen[(MessageNode, State)] =
    sized {
      s =>
        for {
          (myId, state) <- Gen.const(state.nextMessageId)
          (name, state) <- state.generateSubspace
          (messages, state) <- listWithStatefulGen(state, maxSize = (3 - depth) max 0)(genMessageNode(depth + 1, Some(myId), protoSyntax))
          (enums, state) <- listWithStatefulGen(state, maxSize = 3)(genEnumNode(Some(myId), protoSyntax))
          fieldCount <- choose[Int](0, s min 15)
          (fieldNames, state) <- listOfNWithStatefulGen(fieldCount, state)(_.generateName)
          fieldTags <- genListOfDistinctPositiveNumbers(fieldCount)
          (oneOfGroupings, state) <- genOneOfs(fieldCount, state)
          isInOneof = oneOfGroupings.map(_.isOneof)
          fieldTypes <- Gen.sequence[Seq[ProtoType], ProtoType](isInOneof.map(isOneof => GenTypes.genFieldType(state, protoSyntax, allowMaps = !isOneof)))
          fieldOptions <- Gen.sequence[Seq[FieldOptions], FieldOptions](
            (fieldTypes zip isInOneof).map {
              case (fieldType, inOneof) =>
                GenTypes.genOptionsForField(myId, fieldType, protoSyntax, inOneof = inOneof)
            })
          fields = (fieldNames zip oneOfGroupings) zip ((fieldTypes, fieldOptions, fieldTags).zipped).toList map {
            case ((n, oog), (t, opts, tag)) => FieldNode(n, t, opts, oog, tag)
          }
        } yield (MessageNode(myId, name, messages, enums, fields, parentMessageId,
          state.currentFileId), state.closeNamespace)
    }

  def genScalaOptions(state: State): Gen[(Option[ScalaPbOptions], State)] = for {
    (scalaPackageName, state) <- GenUtils.listWithStatefulGen(state, minSize = 1, maxSize = 4)(_.generateName)
    flatPackage <- Gen.oneOf(true, false)
  } yield {
    val b = ScalaPbOptions.newBuilder
    if (scalaPackageName.nonEmpty) {
      b.setPackageName(scalaPackageName.mkString("."))
    }
    b.setFlatPackage(flatPackage)
    (Some(b.build), state)
  }

  val genStreamType: Gen[StreamType] = Gen.oneOf(
    StreamType.Unary, StreamType.ClientStreaming, StreamType.ServerStreaming, StreamType.Bidirectional
  )

  def genService(messages: Seq[MessageNode])(state: State): Gen[(ServiceNode, State)] = for{
    (methods, state) <- listWithStatefulGen(state , maxSize = 3)(genMethod(messages))
    (name, state) <- state.generateName
  } yield ServiceNode(name, methods) -> state

  def genMethod(messages: Seq[MessageNode])(state: State): Gen[(MethodNode, State)] = for{
    req <- Gen.oneOf(messages)
    res <- Gen.oneOf(messages)
    stream <- genStreamType
    (name, state) <- state.generateName
  } yield MethodNode(name, req, res, stream) -> state

  def genFileNode(state: State): Gen[(FileNode, State)] = sized {
    s =>
      for {
        (baseName, fileId, state) <- state.newFile
        protoSyntax <- Gen.oneOf[ProtoSyntax](Proto2, Proto3)
        (javaPackageNames, state) <- GenUtils.listWithStatefulGen(state, minSize = 1, maxSize = 4)(_.generateName)
        javaPackage = javaPackageNames mkString "."
        javaPackageOption = if (javaPackage.nonEmpty) Some(javaPackage) else None
        (scalaOptions, state) <- Gen.oneOf[(Option[ScalaPbOptions], State)](genScalaOptions(state), (None, state))
        (protoPackage, state) <- Gen.oneOf(state.generateSubspace, Gen.const(("", state)))
        protoPackageOption = if (protoPackage.nonEmpty) Some(protoPackage) else None
        (messages, state) <- listWithStatefulGen(state, maxSize = 4)(genMessageNode(0, None, protoSyntax))
        (enums, state) <- listWithStatefulGen(state, maxSize = 3)(genEnumNode(None, protoSyntax))
        javaMulti <- implicitly[Arbitrary[Boolean]].arbitrary
        (services, state) <- listWithStatefulGen(state, maxSize = 3)(genService(messages))
      } yield (FileNode(baseName, protoSyntax, protoPackageOption, javaPackageOption, javaMulti, scalaOptions, messages, services, enums, fileId),
        if (protoPackage.isEmpty) state else state.closeNamespace)
  }

  def genRootNode: Gen[RootNode] =
    listWithStatefulGen(State(), maxSize = 10)(genFileNode).map {
      case (files, state) =>
        assert(state.namespace.parent.isEmpty)
        RootNode(files)
    }.suchThat(_.maxMessageId.isDefined)
}
