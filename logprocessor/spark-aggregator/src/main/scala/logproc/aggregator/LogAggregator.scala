package logproc.aggregator

import cloudflow.spark.sql.SQLImplicits._
import org.apache.spark.sql.functions._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{DurationConfigParameter, StreamletShape}
import org.apache.spark.sql.{Dataset, Encoder, Encoders}
import org.apache.spark.sql.functions.window
import org.apache.spark.sql.streaming.OutputMode
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import logproc.data._
import logproc.ingestor.LogMessageJsonSupport._
import logproc.ingestor.LogStatsJsonSupport._

import scala.concurrent.duration
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

case class Log(timestamp: String, errorType: String, message: String)

class LogAggregator extends SparkStreamlet{

  val in = AvroInlet[LogMessage]("message-in")
  val out = AvroOutlet[LogStats]("stats-out")
  override def shape(): StreamletShape = StreamletShape(in, out)
//  val shape: StreamletShape = StreamletShape(in, out)

  val GroupByWindow = Some("5 second")// DurationConfigParameter("group-by-window", "Window duration for the moving average computation", Some("5 second"))

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic {

    val groupByWindow = GroupByWindow.value

    override def buildStreamingQueries: StreamletQueryExecution = {
      implicit val enc: Encoder[LogStats] = Encoders.product[LogStats]
      val dataset = readStream(in)
      val outStream = process(dataset)
      writeStream(outStream, out, OutputMode.Append).toQueryExecution
    }

    val check_list = List("ERROR, WARN")
    val threshold = 2
    private def process(inDataset: Dataset[LogMessage]): Dataset[LogStats] = {
      val query =
        inDataset
//          .filter($"logType".isin(check_list))
//          .log("Reading dataset {}".format(col("message")))
          .groupBy(window($"timestamp", s"${Duration.create(200, duration.MILLISECONDS)}"))
          .agg(sum($"1").as("numLogs"), sum(when($"logType".isin(check_list), 1).otherwise(0)).as("numErrors"))
          .withColumn("flagErrors", when($"numErrors">threshold, lit("1")).otherwise(lit("0")))

      query
        .select($"numLogs", $"numErrors", $"flagErrors")
        .as[LogStats]
    }
  }
}