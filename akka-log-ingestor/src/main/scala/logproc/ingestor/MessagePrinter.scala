package logproc.ingestor

import akka.stream.scaladsl.Sink
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._

import logproc.data._

object MessagePrinter extends AkkaStreamlet {
  // 1. Create inlets and outlets
  val inlet = AvroInlet[LogFile]("message-in")

  // 2. Define the shape of the streamlet
  override def shape() = StreamletShape.withInlets(inlet)

  // 3. Override createLogic to provide StreamletLogic
  def createLogic = new RunnableGraphStreamletLogic() {
    def format(message: LogFile) = s"Message is: ${message.content}"
    def runnableGraph =
      plainSource(inlet)
        .to(Sink.foreach(message => println(format(message))))
  }
}