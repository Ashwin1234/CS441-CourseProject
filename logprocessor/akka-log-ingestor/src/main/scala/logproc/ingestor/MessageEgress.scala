package logproc.ingestor

import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic

import logproc.data._

class MessageEgress extends AkkaStreamlet {
  var in = AvroInlet[LogMessage]("in")

  override def shape: StreamletShape = StreamletShape.createWithInlets(in)

  override def createLogic: AkkaStreamletLogic = new RunnableGraphStreamletLogic {
    override def runnableGraph() =
      sourceWithCommittableContext(in).via(
        FlowWithCommittableContext[LogMessage]
          .map { message => {
//            log.info("Received message in egress: {}".format(message.toString))
//            System.out.println(message.toString)
            log.warn("Received message {}".format(message.message))
          }
         }
      ).to(committableSink)
  }
}

