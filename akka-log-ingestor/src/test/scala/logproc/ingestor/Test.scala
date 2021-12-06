package logproc.ingestor

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import com.google.gson.Gson
import logproc.data._
import spray.json.JsonParser

import LogStatsJsonSupport._

case class JsonStat(app: String, windowstart: Long, windowend: Long, numLogs: Long, numErrors: Long, flagErrors: Long)

// Test cases
class Test extends AnyFlatSpec with Matchers {

  behavior of "configuration parameters module"
  val config: Config = ConfigFactory.load("application.conf")

  it should "have the correct bucket name" in {
    val clientRegion: Regions = Regions.US_EAST_1

    val s3Client = AmazonS3ClientBuilder
      .standard()
      .withRegion(clientRegion)
      .withCredentials(new ProfileCredentialsProvider())
      .build()

    s3Client.doesBucketExistV2(config.getString("s3.bucket")) shouldBe true

  }
  it should "have a valid key name" in {
    config.getString("s3.key") shouldBe "logs/new_log.log"
  }
  it should "have the correct region name" in {
    config.getString("s3.region") shouldBe "Regions.US_EAST_1"
  }
  it should "send email successfully" in {
    val gson = new Gson()
    val stat = JsonParser(gson.toJson(JsonStat("test app", 1231, 1234, 5, 10, 1))).convertTo[LogStats]
    val emailProc = new EmailProc
    emailProc.sendEmail(stat) shouldBe "email sent successfully"
  }
  it should "window be atleast 2 seconds" in {
    assert(config.getString("spark.window").split(" ")(0).toInt > 2)
  }

}