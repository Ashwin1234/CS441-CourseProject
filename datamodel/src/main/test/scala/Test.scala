import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
//import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.matchers.should.Matchers.shouldBe


// Test cases
class Test extends AnyFlatSpec with Matchers {

  behavior of "configuration parameters module"
  val config: Config = ConfigFactory.load("application.conf")


  it should "bucket name" in {
    config.getString("s3.bucket") shouldBe "logs-project"
  }
  it should "key name" in {
    config.getString("s3.key") shouldBe "logs/new_log.log"
  }
  it should "region name" in {
    config.getString("s3.region") shouldBe "Regions.US_EAST_1"
  }
  it should "time" in {
    config.getString("kafka.time") shouldBe "1489997145000L"
  }
  it should "window" in {
    config.getString("spark.window") shouldBe "5 second"
  }




}