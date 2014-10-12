import org.scalatest.{OptionValues, Matchers, FlatSpec}
import com.trueaccord.lenses._

case class Person(firstName: String, lastName: String, age: Int, address: Address) extends Updatable[Person]

case class Address(street: String,
                   city: String,
                   state: String,
                   residents: Seq[Person] = Nil) extends Updatable[Address]

case class Role(name: String, person: Person, replacement: Option[Person] = None) extends Updatable[Role]


class SimpleTest extends FlatSpec with Matchers with OptionValues {

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

    def residents = field(_.residents)((p, f) => p.copy(residents = f))
  }

  object RoleMutation extends RoleMutation(Lens.unit)

  val mosh = Person(firstName = "Mosh", lastName = "Ben", age = 19,
    address = Address("Main St.", "San Jose", "CA"))
  val josh = Person(firstName = "Josh", lastName = "Z", age = 19,
    address = Address("Fremont", "Sunnyvale", "CA"))
  val chef = Role(name = "Chef", person=mosh)

  "update" should "return an updated object" in {
    mosh.update(_.firstName := "foo") should be(mosh.copy(firstName = "foo"))
  }

  it should "allow mutating nested fields" in {
    mosh.update(_.address.city := "Valejo") should be(mosh.copy(address = mosh.address.copy(city = "Valejo")))
  }

  it should "allow replacing an entire field" in {
    val portland = Address("2nd", "Portland", "Oregon")
    mosh.update(_.address := portland) should be(mosh.copy(address = portland))
  }

  it should "allow adding to a sequence" in {
    mosh.update(_.address.residents :+= josh) should be(
      mosh.copy(
        address = mosh.address.copy(
          residents = mosh.address.residents :+ josh)))
  }

  it should "allow replacing a sequence" in {
    mosh.update(_.address.residents := Seq(josh, mosh)) should be(
      mosh.copy(address =
        mosh.address.copy(residents = Seq(josh, mosh))))
  }

  it should "allow mutating an element of a sequence by index" in {
    mosh.update(
      _.address.residents := Seq(josh, mosh),
      _.address.residents(1).firstName := "ModName") should be(
      mosh.copy(
        address = mosh.address.copy(
          residents = Seq(josh, mosh.copy(firstName = "ModName")))))
  }

  it should "allow mutating all element of a sequence with forEach" in {
    mosh.update(
      _.address.residents := Seq(josh, mosh),
      _.address.residents.foreach(_.lastName.modify(_ + "Suffix"))) should be(
      mosh.copy(
        address = mosh.address.copy(
          residents = Seq(
            josh.copy(lastName = "ZSuffix"),
            mosh.copy(lastName = "BenSuffix")))))
  }

  it should "allow mapping over an option" in {
    chef.update(
      _.replacement.inplaceMap(_.firstName := "Zoo")
    ) should be(chef)

    chef.update(
      _.replacement := Some(josh),
      _.replacement.inplaceMap(_.firstName := "Yosh")
    ).replacement.value should be(josh.copy(firstName = "Yosh"))
  }
}

