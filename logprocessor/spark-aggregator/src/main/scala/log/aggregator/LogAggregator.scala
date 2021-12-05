package log.aggregator

import cloudflow.spark.sql.SQLImplicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{DurationConfigParameter, StreamletShape}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.window
import org.apache.spark.sql.streaming.OutputMode

import log.ingestor._

class LogAggregator extends SparkStreamlet{

  val in = AvroInlet[LogMessage]("message-in")
  val out = AvroOutlet[LogStats](name="stats-out")
  override def shape(): StreamletShape = StreamletShape.withInlets(in)

  val GroupByWindow = DurationConfigParameter("group-by-window", "Window duration for the moving average computation", Some("1 minute"))

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic {

    val groupByWindow = GroupByWindow.value

    override def buildStreamingQueries: StreamletQueryExecution = {
      val dataset = readStream(in)
      val outStream = process(dataset)
      writeStream(outStream, out, OutputMode.Append).toQueryExecution
    }

    private def process(inDataset: Dataset[LogMessage]): Dataset[LogStats] = {
      val query =
        inDataset
          .groupBy(window($"ts", s"${groupByWindow.toMillis()} milliseconds"))
          .agg(sum($"1").as("numLogs"))

      query
        .select($"numLogs")
        .as[LogStats]
    }
  }
}
