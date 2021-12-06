package logproc.ingestor

import com.typesafe.config.ConfigFactory
import spray.json._

import java.util.UUID
import scala.util.Try
import logproc.data._
// JsonSupport for different AVROs used
trait UUIDJsonSupport extends DefaultJsonProtocol {
  val conf = ConfigFactory.load()
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(json: JsValue): UUID = json match {
      case JsString(uuid) => {
        val conf   = ConfigFactory.load("application.conf")
        Try(UUID.fromString(uuid)).getOrElse(deserializationError(conf.getString("jsonSupport.deSerialisationError")))
      }
      case other          => deserializationError(conf.getString("jsonSupport.deSerialisationError"))
    }
  }
}

object LogKeyJsonSupport extends DefaultJsonProtocol with UUIDJsonSupport {
  implicit val logUrlFormat = jsonFormat(LogKey.apply, "appId", "key")
}

object LogMessageJsonSupport extends DefaultJsonProtocol {
  implicit val logMessageFormat = jsonFormat3(LogMessage.apply)
}

object LogFileJsonSupport extends DefaultJsonProtocol {
  implicit val logFileFormat = jsonFormat2(LogFile.apply)
}

object LogStatsJsonSupport extends DefaultJsonProtocol {
  implicit val logStatFormat = jsonFormat6(LogStats.apply)
}
