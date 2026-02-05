import com.thesamet.proto.e2e.enum_alias._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class EnumAliasSpec extends AnyFlatSpec with Matchers with OptionValues {
  "aliased enums" should "be equal" in {
    EnumAllowingAlias.EAA_RUNNING must be(EnumAllowingAlias.EAA_STARTED)
  }

  it should "report isX true for all aliases" in {
    EnumAllowingAlias.EAA_RUNNING.isEaaRunning must be(true)
    EnumAllowingAlias.EAA_RUNNING.isEaaStarted must be(true)
    EnumAllowingAlias.EAA_FINISHED.isEaaStarted must be(false)
    EnumAllowingAlias.EAA_FINISHED.isEaaRunning must be(false)
  }
}
