package logproc.aggregator

import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{Encoder, Encoders}

import logproc.data._

class LogConsoleEgress extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in    = AvroInlet[LogStats]("stats-in")
  override def shape = StreamletShape(in)

  override def createLogic = new SparkStreamletLogic {
    implicit val enc: Encoder[LogStats] = Encoders.product[LogStats]
    override def buildStreamingQueries =
      readStream(in).writeStream
        .format("console")
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
  }
}
