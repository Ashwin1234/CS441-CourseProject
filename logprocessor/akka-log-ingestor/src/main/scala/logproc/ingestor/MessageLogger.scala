package logproc.ingestor

import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet

import logproc.data._

class MessageLogger extends LoggerStreamlet[LogMessage]("Message Read received: {}", "message-in")

//class MessageLogger extends SparkStreamlet {
//
//  val rootLogger = Logger.getRootLogger()
//  rootLogger.setLevel(Level.ERROR)
//
//  val in    = AvroInlet[LogMessage]("message-in")
//  override def shape() = StreamletShape(in)
//
//  override def createLogic = new SparkStreamletLogic {
//    implicit val enc: Encoder[LogMessage] = Encoders.product[LogMessage]
//    override def buildStreamingQueries =
//      readStream(in).writeStream
//        .format("console")
//        .outputMode(OutputMode.Append())
//        .start()
//        .toQueryExecution
//  }
//}
