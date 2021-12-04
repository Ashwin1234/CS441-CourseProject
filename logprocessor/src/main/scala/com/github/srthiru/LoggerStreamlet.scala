package com.github.srthiru

import akka.event.Logging
import cloudflow.akkastream.AkkaStreamlet
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import org.apache.avro.specific.SpecificRecordBase

import scala.reflect.ClassTag

abstract class LoggerStreamlet[T <: SpecificRecordBase: ClassTag](template: String,
                                                                  inletName: String,
                                                                  logLevel: Logging.LogLevel = Logging.InfoLevel)
    extends AkkaStreamlet {
  val inlet            = AvroInlet[T](name = inletName)
  override def shape() = StreamletShape.withInlets(inlet)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph =
      sourceWithCommittableContext(inlet)
        .map { element =>
          system.log.log(logLevel, template, element)
          element
        }
        .to(committableSink)
  }
}
