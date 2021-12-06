package logproc.ingestor

//import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Source
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.akkastream.util.scaladsl.Splitter
//
//import akka.stream.scaladsl.RunnableGraph
//import cloudflow.akkastream.scaladsl.{FlowWithCommittableContext, RunnableGraphStreamletLogic}
//import cloudflow.akkastream.util.scaladsl.Splitter
//import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
//import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
//import cloudflow.streamlets.{CodecOutlet, RoundRobinPartitioner, StreamletShape}

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import spray.json.JsonParser

import logproc.data._

/*
An AkkaStreamlet that reads a file from S3 based on the key received through the inlet.

Streamlet Specs:

Inlets    : "key-in" Receives the request passed on by the LogHttpIngress
Outlets   : "messages-out" Sends out a stream of log messages to the MessageLogger and MessageAggregator
Logic     : RunnableGraphStreamletLogic to execute the runnableGraph that reads the file from S3 and streams the messages
 */
class LogFileIngestor extends AkkaStreamlet {
  import LogMessageJsonSupport._
  import LogFileJsonSupport._

  val in  = AvroInlet[LogKey]("key-in")
//  val left = AvroOutlet[LogMessage]("message-valid")
//  val right: AvroOutlet[InvalidLog] = AvroOutlet[InvalidLog]("message-invalid")
  val middle = AvroOutlet[LogFile]("message-valid")

  case class JsonLogMessage(timestamp: Long, logType: String, message: String)
  case class JsonLogFile(app: String, content: String)
  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(middle)

  /*
  Function to read log messages from the given S3 Key

  @param key: String - S3 key of the log file to be read
  */
  def readLog(key: String): LogFile = {
    val conf   = ConfigFactory.load()
    val bucket = conf.getString("s3.bucket")

    import com.amazonaws.regions.Regions

    val clientRegion: Regions = Regions.US_EAST_1

    val s3Client = AmazonS3ClientBuilder
      .standard()
      .withRegion(clientRegion)
      .withCredentials(new ProfileCredentialsProvider())
      .build()

    val s3_object = s3Client.getObject(new GetObjectRequest(bucket, key))
    val output = scala.io.Source.fromInputStream(s3_object.getObjectContent).mkString
    val app = key.split("/")(1).split("\\.")(0)
    val gson   = new Gson
    val jsonVal      = JsonParser(gson.toJson(JsonLogFile(app, output)))
    log.warn("Parsed Whole message: {}" + jsonVal)
    val convertedVal = jsonVal.convertTo[LogFile]
    log.warn("Converted Whole message: {}"+ convertedVal)
    convertedVal
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {

    override def runnableGraph() = {
      sourceWithCommittableContext(in).map(key => readLog(key.key)).to(committableSink(middle))
    }

  }

}
