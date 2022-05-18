package scalapb.lenses

import munit.FunSuite

case class Person(firstName: String, lastName: String, age: Int, address: Address)
    extends Updatable[Person]

case class Address(street: String, city: String, state: String, residents: Seq[Person] = Nil)
    extends Updatable[Address]

case class Role(name: String, person: Person, replacement: Option[Person] = None)
    extends Updatable[Role]

case class MapTest(
    intMap: Map[Int, String] = Map.empty,
    nameMap: Map[String, Person] = Map.empty,
    addressMap: Map[Person, Address] = Map.empty
) extends Updatable[MapTest]

case class CollectionTypes(
    iSeq: collection.immutable.Seq[String] = Nil,
    vector: Vector[String] = Vector.empty,
    list: List[String] = Nil,
    sett: Set[String] = Set.empty
) extends Updatable[CollectionTypes]

class SimpleTest extends FunSuite {
  implicit class RoleMutation[U](f: Lens[U, Role]) extends ObjectLens[U, Role](f) {
    def name = field(_.name)((p, f) => p.copy(name = f))

    def person = field(_.person)((p, f) => p.copy(person = f))

    def replacement = field(_.replacement)((p, f) => p.copy(replacement = f))
  }

  implicit class PersonMutation[U](f: Lens[U, Person]) extends ObjectLens[U, Person](f) {
    def firstName = field(_.firstName)((p, f) => p.copy(firstName = f))

    def lastName = field(_.lastName)((p, f) => p.copy(lastName = f))

    def address: Lens[U, Address] = field(_.address)((p, f) => p.copy(address = f))
  }

  implicit class AddressLens[U](val f: Lens[U, Address]) extends ObjectLens[U, Address](f) {
    def city = field(_.city)((p, f) => p.copy(city = f))

    def street = field(_.street)((p, f) => p.copy(street = f))

    def residents: Lens[U, Seq[Person]] = field(_.residents)((p, f) => p.copy(residents = f))
  }

  implicit class MapTestLens[U](val f: Lens[U, MapTest]) extends ObjectLens[U, MapTest](f) {
    def intMap = field(_.intMap)((p, f) => p.copy(intMap = f))

    def nameMap = field(_.nameMap)((p, f) => p.copy(nameMap = f))

    def addressMap = field(_.addressMap)((p, f) => p.copy(addressMap = f))
  }

  implicit class CollectionTypesLens[U](val f: Lens[U, CollectionTypes])
      extends ObjectLens[U, CollectionTypes](f) {
    def iSeq = field(_.iSeq)((p, f) => p.copy(iSeq = f))

    def vector = field(_.vector)((p, f) => p.copy(vector = f))

    def list = field(_.list)((p, f) => p.copy(list = f))

    def sett = field(_.sett)((p, f) => p.copy(sett = f))
  }

  object RoleMutation extends RoleMutation(Lens.unit)

  val mosh = Person(
    firstName = "Mosh",
    lastName = "Ben",
    age = 19,
    address = Address("Main St.", "San Jose", "CA")
  )
  val josh = Person(
    firstName = "Josh",
    lastName = "Z",
    age = 19,
    address = Address("Fremont", "Sunnyvale", "CA")
  )
  val chef = Role(name = "Chef", person = mosh)

  val mapTest = MapTest(
    intMap = Map(3 -> "three", 4 -> "four"),
    addressMap = Map(mosh -> Address("someStreet", "someCity", "someState"))
  )

  test("update should return an updated object") {
    assertEquals(
      mosh.update(_.firstName := "foo"),
      (mosh.copy(firstName = "foo"))
    )
  }

  test("it should allow mutating nested fields") {
    assertEquals(
      mosh.update(_.address.city := "Valejo"),
      (mosh.copy(
        address = mosh.address.copy(city = "Valejo")
      ))
    )
  }

  test("it should allow nested updates") {
    assertEquals(
      mosh.update(
        _.address.update(
          _.city   := "Valejo",
          _.street := "Fourth"
        )
      ),
      (mosh.copy(address = mosh.address.copy(city = "Valejo", street = "Fourth")))
    )
  }

  test("it should allow replacing an entire field") {
    val portland = Address("2nd", "Portland", "Oregon")
    assertEquals(mosh.update(_.address := portland), (mosh.copy(address = portland)))
  }

  test("it should support an existing value for an optional set") {
    assertEquals(mosh.update(_.firstName setIfDefined Some("foo")), mosh.copy(firstName = "foo"))
  }

  test("it should support a non-existing value for an optional set") {
    assertEquals(mosh.update(_.firstName setIfDefined None), mosh)
  }

  test("it should allow adding to a sequence") {
    assertEquals(
      mosh.update(_.address.residents :+= josh),
      (mosh.copy(
        address = mosh.address.copy(residents = mosh.address.residents :+ josh)
      ))
    )
  }

  test("it should allow replacing a sequence") {
    assertEquals(
      mosh.update(_.address.residents := Seq(josh, mosh)),
      (mosh.copy(
        address = mosh.address.copy(residents = Seq(josh, mosh))
      ))
    )
  }

  test("it should allow mutating an element of a sequence by index") {
    assertEquals(
      mosh.update(
        _.address.residents              := Seq(josh, mosh),
        _.address.residents(1).firstName := "ModName"
      ),
      (mosh.copy(
        address = mosh.address.copy(residents = Seq(josh, mosh.copy(firstName = "ModName")))
      ))
    )
  }

  test("it should allow mutating all element of a sequence with forEach") {
    assertEquals(
      mosh.update(
        _.address.residents := Seq(josh, mosh),
        _.address.residents.foreach(_.lastName.modify(_ + "Suffix"))
      ),
      (mosh.copy(
        address = mosh.address
          .copy(residents = Seq(josh.copy(lastName = "ZSuffix"), mosh.copy(lastName = "BenSuffix")))
      ))
    )
  }

  test("it should allow mapping over an option") {
    assertEquals(
      chef.update(
        _.replacement.inplaceMap(_.firstName := "Zoo")
      ),
      chef
    )

    assertEquals(
      chef
        .update(
          _.replacement := Some(josh),
          _.replacement.inplaceMap(_.firstName := "Yosh")
        )
        .replacement
        .get,
      (josh.copy(firstName = "Yosh"))
    )
  }

  test("it should allow updating a map") {
    assertEquals(
      mapTest.update(_.intMap(5) := "hello"),
      (mapTest.copy(
        intMap = mapTest.intMap.updated(5, "hello")
      ))
    )
    assertEquals(
      mapTest.update(_.intMap(2) := "ttt"),
      (mapTest.copy(
        intMap = mapTest.intMap.updated(2, "ttt")
      ))
    )
    assertEquals(
      mapTest.update(_.nameMap("mmm") := mosh),
      (mapTest.copy(
        nameMap = mapTest.nameMap.updated("mmm", mosh)
      ))
    )
    assertEquals(
      mapTest.update(_.addressMap(josh) := mosh.address),
      (mapTest.copy(
        addressMap = mapTest.addressMap.updated(josh, mosh.address)
      ))
    )
  }

  test("it should allow nested updated in a map") {
    assertEquals(
      mapTest.update(_.nameMap("mosh") := mosh, _.nameMap("mosh").firstName := "boo"),
      (mapTest
        .copy(nameMap = mapTest.nameMap.updated("mosh", mosh.copy(firstName = "boo"))))
    )
  }

  test("it should raise an exception on nested key update for a missing key") {
    intercept[NoSuchElementException] {
      mapTest.update(
        _.nameMap("mosh").firstName := "Boo"
      )
    }
  }

  test("it should allow transforming the map values with forEachValue") {
    assertEquals(
      mapTest
        .update(
          _.nameMap("mosh") := mosh,
          _.nameMap("josh") := josh,
          _.nameMap.foreachValue(_.firstName := "ttt")
        )
        .nameMap
        .values
        .map(_.firstName)
        .toSeq,
      (Seq("ttt", "ttt"))
    )
  }

  test("it should allow transforming the map values with mapValues") {
    assertEquals(
      mapTest
        .update(
          _.intMap.mapValues("hello " + _)
        )
        .intMap,
      (Map(3 -> "hello three", 4 -> "hello four"))
    )

    assertEquals(
      mapTest
        .update(
          _.nameMap("mosh") := mosh,
          _.nameMap("josh") := josh,
          _.nameMap.mapValues(m => m.update(_.firstName := "*" + m.firstName))
        )
        .nameMap
        .values
        .map(_.firstName)
        .toSeq,
      (Seq("*Mosh", "*Josh"))
    )
  }

  test("it should allow transforming the map values with forEach") {
    assertEquals(
      mapTest.update(_.intMap.foreach(_.modify(k => (k._1 - 1, "*" + k._2)))).intMap,
      Map(
        2 -> "*three",
        3 -> "*four"
      )
    )
  }

  test("it should support other collection types") {
    val ct = CollectionTypes().update(
      _.iSeq := collection.immutable.Seq("3", "4", "5"),
      _.iSeq :+= "foo",
      _.iSeq :++= collection.immutable.Seq("6", "7", "8"),
      _.iSeq :++= Seq("6", "7", "8"),
      _.iSeq(5) := "11",
      _.vector  := Vector("3", "4", "5"),
      _.vector :+= "foo",
      _.vector :++= collection.immutable.Seq("6", "7", "8"),
      _.vector :++= Seq("6", "7", "8"),
      _.vector(5) := "11",
      _.list      := List("3", "4", "5"),
      _.list :+= "foo",
      _.list :++= collection.immutable.Seq("6", "7", "8"),
      _.list :++= Seq("6", "7", "8"),
      _.list(5) := "11",
      _.sett    := Set("3", "4", "5"),
      _.sett :+= "foo",
      _.sett :++= collection.immutable.Seq("6", "7", "8"),
      _.sett :++= Seq("6", "7", "8")
    )
    val expected = Seq("3", "4", "5", "foo", "6", "11", "8", "6", "7", "8")
    assertEquals(ct.iSeq.toVector, expected.toVector)
    assertEquals(ct.vector, expected.toVector)
    assertEquals(ct.list, expected.toList)
  }

  test("it should work with zipped lenses") {
    assertEquals(
      CollectionTypes().update(k => k.list zip k.vector := ((List("3", "4"), Vector("x", "y")))),
      CollectionTypes(
        list = List("3", "4"),
        vector = Vector("x", "y")
      )
    )
  }
}
