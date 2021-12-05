package logproc.aggregator

import cloudflow.spark.sql.SQLImplicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{DurationConfigParameter, StreamletShape}
import org.apache.spark.sql.{Dataset, Encoder, Encoders}
import org.apache.spark.sql.functions.window
import org.apache.spark.sql.streaming.OutputMode
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import logproc.data._

import scala.reflect.ClassTag

case class Log(timestamp: String, errorType: String, message: String)

class LogAggregator extends SparkStreamlet{

  val in = AvroInlet[LogMessage]("message-in")
  val out = AvroOutlet[LogStats]("stats-out")
  override def shape(): StreamletShape = StreamletShape(in, out)
//  val shape: StreamletShape = StreamletShape(in, out)

  val GroupByWindow = DurationConfigParameter("group-by-window", "Window duration for the moving average computation", Some("1 minute"))

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic {

    val groupByWindow = GroupByWindow.value

    override def buildStreamingQueries: StreamletQueryExecution = {
      implicit val enc: Encoder[LogStats] = Encoders.product[LogStats]
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
