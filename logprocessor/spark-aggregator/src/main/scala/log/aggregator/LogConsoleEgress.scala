package log.aggregator

import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.streaming.OutputMode

class LogConsoleEgress extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in    = AvroInlet[LogStats]("in")
  override def shape = StreamletShape(in)

  override def createLogic = new SparkStreamletLogic {
    override def buildStreamingQueries =
      readStream(in).writeStream
        .format("console")
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
  }
}
