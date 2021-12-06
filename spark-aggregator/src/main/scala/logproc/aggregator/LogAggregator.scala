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
import org.apache.spark.sql.types.{LongType, TimestampType}

import scala.concurrent.duration
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

case class Log(timestamp: String, errorType: String, message: String)

/*
LogAggregator - Spark streamlet logic that processes the log message and generates log stats
 */
class LogAggregator extends SparkStreamlet{

  //Input Avro
  val in = AvroInlet[LogFile]("message-in")
  //Output Avro
  val out = AvroOutlet[LogStats]("stats-out")
  override def shape(): StreamletShape = StreamletShape(in, out)


  val GroupByWindow = Some("spark.window")

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic {

    val groupByWindow = GroupByWindow.value

    /*
    Function buildStreamingQueries - Entry point for streamlet; builds the query and executes with the input avro
     */
    override def buildStreamingQueries: StreamletQueryExecution = {
      implicit val enc: Encoder[LogStats] = Encoders.product[LogStats]
      val dataset = readStream(in)
      //Applying Spark SQL logic
      val outStream = process(dataset)
      //Writing the Stream to the next kafka stream
      writeStream(outStream, out, OutputMode.Append).toQueryExecution
    }

    // List of messages to be checked.
    val check_list = List("ERROR, WARN")
    val threshold = config.getInt("spark.alertThreshold")
    val filterText = config.getString("spark.filterText")

    private def process(inDataset: Dataset[LogFile]): Dataset[LogStats] = {

      //Spark SQL logic
      val query =
        inDataset
          .withColumn("message", explode(split($"content", "[\n]"))) // Splitting the message into log messages
          .where(not($"message".like("[run-main-0]"))) // Ignoring the header log message
          .withColumn("_tmp", split($"message", " ")) // Getting the type, timestamp and message
          .withColumn("timestamp", unix_timestamp($"_tmp".getItem(0), "HH:mm:ss.SSS").cast(TimestampType)) // Timestamp
          .withColumn("logType", $"_tmp".getItem(2)) // Getting the log type
          .withColumn("message", $"_tmp".getItem(5)) // Getting the log messgae
          .withWatermark("timestamp", "0 seconds")
          .groupBy($"app", window($"timestamp", s"${Duration.create(30, duration.SECONDS)}"))
          .agg(count($"message").as("numLogs"), sum(when($"logType" === "WARN" || "$logType" == "ERROR", 1).otherwise(0)).as("numErrors")) // If error/ warn found, compute how many
          .withColumn("flagErrors", when($"numErrors">threshold, 1).otherwise(0)) // If no found, 0
          .withColumn("windowstart", $"window.start".cast(LongType))
          .withColumn("windowend", $"window.end".cast(LongType))

      query
        .select($"app", $"windowstart", $"windowend", $"numLogs", $"numErrors", $"flagErrors") // Output fields
        .as[LogStats]
    }
  }
}