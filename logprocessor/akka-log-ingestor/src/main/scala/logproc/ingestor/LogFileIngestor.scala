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
  import WholeMessageJsonSupport._

  val in  = AvroInlet[LogKey]("key-in")
  val left = AvroOutlet[LogMessage]("message-valid")
//  val right: AvroOutlet[InvalidLog] = AvroOutlet[InvalidLog]("message-invalid")

  val middle = AvroOutlet[WholeMessage]("message-whole")

  case class JsonLogMessage(timestamp: Long, logType: String, message: String)
  case class JsonWholeMessage(message: String)
  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(left) //, right
//  implicit val entityStreamingSupport = EntityStreamingSupport.json()

  def readMessages(key: String) = {

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

    def convertToLogMessage(message: String, key: String): LogMessage = {
      //        log.info("Received: {}", message)
      val splits = message.split(' ')

      // Create timestamp value using the date from the key and the timestamp in the log message
      //        val date = key.split('/')(2).split('.')(1).split('-').slice(0,3).mkString("-")
      //        val time = splits(0)
      //        val format = new java.text.SimpleDateFormat("yyyy-mm-dd HH:m:ss.S")
      //        val ts = format.parse(date+ ' ' + time).getTime()
      val ts = 1489997145000L
      val gson   = new Gson
      //        val jsonLogMessage: JsonLogMessage =
      //        log.info("jsonLogMessage: {}", jsonLogMessage)
      //        val gsonLogMessage =
      //        log.info("gsonLogMessage: {}", gsonLogMessage)
      val jsonVal      = JsonParser(gson.toJson(JsonLogMessage(ts, splits(1), splits(splits.length - 1))))
      val convertedVal = jsonVal.convertTo[LogMessage]
      log.info("parsed: {}, converted: {}" + "--" + jsonVal + "--" + convertedVal)
      convertedVal
    }

    val output   = scala.io.Source.fromInputStream(s3_object.getObjectContent).mkString
    val messages = output.split("\n").map(message => convertToLogMessage(message, key))
    messages
  }

  def readLog(key: String): WholeMessage = {
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
    val gson   = new Gson
    val jsonVal      = JsonParser(gson.toJson(JsonWholeMessage(output)))
    log.info("Parsed Whole message: {}".format(jsonVal))
    val convertedVal = jsonVal.convertTo[WholeMessage]
    log.info("Converted Whole message: {}".format(convertedVal))
    convertedVal
  }

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {
    //    val key = conf.getString("s3.key")

//    def validationFlow =
//      FlowWithCommittableContext[LogMessage]
//        .map { logval =>
//          if (logval.message.contains("records.")) Left(InvalidLog(logval.toString, "Invalid log message"))
//          else Right(LogMessage(logval.timestamp, logval.logType, logval.message))
//        }

    override def runnableGraph() = {

      sourceWithCommittableContext(in).map(key => readMessages(key.key)).map(message =>
        message.map(eachMessage => {
          log.info("Single message: {}" + eachMessage.message)
          Source.single(eachMessage).to(plainSink(left))
        })
//
      ).to(committableSink)
    } //.asInstanceOf[CodecOutlet[java.io.Serializable]]



//    sourceWithCommittableContext(in).map(key => readLog(key.key)).to(committableSink(middle)) //.asInstanceOf[CodecOutlet[java.io.Serializable]]

  }

}
