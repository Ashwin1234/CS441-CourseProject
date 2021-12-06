package logproc.ingestor

import com.typesafe.config.ConfigFactory
import logproc.data._
// Streamlet for logging the key
class KeyLogger extends LoggerStreamlet[LogKey](ConfigFactory.load("application.conf").getString("KeyLogger.message"), ConfigFactory.load("application.conf").getString("KeyLogger.inletName"))
