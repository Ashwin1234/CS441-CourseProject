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
import com.typesafe.config.ConfigFactory
import logproc.data._

class EmailProc extends AkkaStreamlet{

  val conf   = ConfigFactory.load("application.conf")
  val in = AvroInlet[LogStats](conf.getString("email.inlet-name"))

  def sendEmail(stat: LogStats) = {
    if(stat.flagErrors == 1){
      val from = conf.getString("email.emailId")
      val to = conf.getString("email.emailId")
      val HTMLBODY = conf.getString("email.body")
      val SUBJECT = conf.getString("email.subject")

      val client = AmazonSimpleEmailServiceClientBuilder
        .standard()
        .withRegion(Regions.US_EAST_1).build()
      val request = new SendEmailRequest()
        .withDestination(new Destination().withToAddresses(to))
        .withMessage(new Message().
          withBody(new Body()
            .withHtml(new Content().withCharset(conf.getString("email.charset"))
              .withData(HTMLBODY)))
          withSubject(new Content()
          .withCharset(conf.getString("email.charset")).withData(SUBJECT)))
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
