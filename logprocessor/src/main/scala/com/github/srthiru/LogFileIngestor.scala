package com.github.srthiru

import akka.NotUsed
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.stream.alpakka.s3.ObjectMetadata
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{ JsonFraming, RunnableGraph, Sink, Source }
import akka.util.ByteString
import cloudflow.akkastream.javadsl.FlowWithCommittableContext
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.{ CodecOutlet, RoundRobinPartitioner, StreamletShape }
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import com.typesafe.config.ConfigFactory
import spray.json.JsonParser

import scala.concurrent.duration.DurationInt
import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl._
import akka.util.ByteString
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.google.gson.Gson
import spray.json.JsonParser

import scala.concurrent.Future

/*
An AkkaStreamlet that reads a file from S3 based on the key received through the inlet.

Streamlet Specs:

Inlets    : "key-in" Receives the request passed on by the LogHttpIngress
Outlets   : "messages-out" Sends out a stream of log messages to the MessageLogger and MessageAggregator
Logic     : RunnableGraphStreamletLogic to execute the runnableGraph that reads the file from S3 and streams the messages
 */
class LogFileIngestor extends AkkaStreamlet {

  import LogMessageJsonSupport._

  val in  = AvroInlet[LogKey]("key-in")
  val out = AvroOutlet[LogMessage]("message-out").withPartitioner(RoundRobinPartitioner)

  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(out)

  implicit val entityStreamingSupport = EntityStreamingSupport.json()
  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {

    def generateMessages(key: String) = {

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

      case class JsonLogMessage(timestamp: String, logType: String, message: String)

      def convertToLogMessage(message: String): LogMessage = {
        val splits  = message.split(' ')
        val gson    = new Gson
        val jsonVal = JsonParser(gson.toJson(JsonLogMessage(splits(0), splits(1), splits(splits.length - 1))))
        jsonVal.convertTo[LogMessage]
      }

      val output   = scala.io.Source.fromInputStream(s3_object.getObjectContent).mkString
      val messages = output.split("\n").map(message => convertToLogMessage(message))
      messages
    }
//    val key = conf.getString("s3.key")

    override def runnableGraph(): RunnableGraph[_] =
      plainSource(in).map(key => generateMessages(key.key)).to(plainSink(out.asInstanceOf[CodecOutlet[java.io.Serializable]]))

  }

}
