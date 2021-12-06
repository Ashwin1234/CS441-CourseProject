package logproc.ingestor

import akka.stream.scaladsl.RunnableGraph
import cloudflow.akkastream.javadsl.FlowWithCommittableContext
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import logproc.data._

class EmailProc extends AkkaStreamlet{

  val in = AvroInlet[LogStats]("stats-in")

  def sendEmail(stat: LogStats) = {
    if(stat.flagErrors == 1){
      val from = "skrish45@uic.edu"
      val to = "skrish45@uic.edu"
      val HTMLBODY = "<p>Oops! Your application failed</p>"
      val SUBJECT = "Application update"

      val client = AmazonSimpleEmailServiceClientBuilder
        .standard()
        .withRegion(Regions.US_EAST_1).build()
      val request = new SendEmailRequest()
        .withDestination(new Destination().withToAddresses(to))
        .withMessage(new Message().
          withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8")
              .withData(HTMLBODY)))
          withSubject(new Content()
          .withCharset("UTF-8").withData(SUBJECT)))
        .withSource(from)

      client.sendEmail(request)
      stat
    }
  }


  override def shape(): StreamletShape = StreamletShape(in)
  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {
    override def runnableGraph(): RunnableGraph[_] =
      sourceWithCommittableContext(in).map(stat => sendEmail(stat)).to(committableSink)
  }

}
