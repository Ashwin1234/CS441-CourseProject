package logproc.ingestor

import akka.http.scaladsl.common.EntityStreamingSupport
import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.akkastream.{AkkaServerStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{CodecOutlet, StreamletShape}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import logproc.data._

/*
An AkkaServerStreamlet that serves as the starting point of the application.

It acts as a HTTP server waiting to process the requests it receives. Usually hosted in port 3000.

Streamlet Specs:

Inlets    : None #Ingress has no inlets
Outlets   : "key-out" Sends the received S3 key request to the LogFileIngestor and the KeyLogger
Logic     : HTTPServerLogic to pass along the received request to the next outlet
 */
class LogHttpIngress extends AkkaServerStreamlet {
  import LogKeyJsonSupport._
  // Outlet with a round robin partitioner
  val out = AvroOutlet[LogKey]("key-out")

  // Defining the streamlet shape
  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  // Streaming support for Json processing of the received requests
//  implicit val entityStreamingSupport = EntityStreamingSupport.json()

  // Logic of the streamlet
  override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, out)

//    HttpServerLogic.default(this, out)
}
