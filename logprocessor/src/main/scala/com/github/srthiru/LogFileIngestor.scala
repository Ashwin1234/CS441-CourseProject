package com.github.srthiru

import akka.NotUsed
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.stream.alpakka.s3.ObjectMetadata
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{JsonFraming, RunnableGraph, Sink, Source}
import akka.util.ByteString
import cloudflow.akkastream.javadsl.FlowWithCommittableContext
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.{CodecOutlet, RoundRobinPartitioner, StreamletShape}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import com.typesafe.config.ConfigFactory
import spray.json.JsonParser

import scala.concurrent.duration.DurationInt

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

    val conf   = ConfigFactory.load()
    val bucket = conf.getString("s3-bucket")
    val key    = conf.getString("s3-key")

    val readFile: _ => Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] = { _ =>
      S3.download(bucket, key).runWith(Sink.head)
    }

    val parseFile: Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] => LogMessage = { source =>
      JsonParser(source.runWith(Sink.head).futureValue.utf8String).convertTo[LogMessage](StringJsonFormat)
    }

    val emitOut = Source.tick(0.second, 200.second, NotUsed).map(_ => readFile).map(parseFile)


//    val s3File = S3.download(bucket, key)
//
//    def flow =
//      FlowWithCommittableContext[]
//        .map { message: LogMessage =>
//          system.log.info(s"Received message: $message")
//        }

    override def runnableGraph(): RunnableGraph[_] =
      s3File.to(committableSink(out))
  }

}
