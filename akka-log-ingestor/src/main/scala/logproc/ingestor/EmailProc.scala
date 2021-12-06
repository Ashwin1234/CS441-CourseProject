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

  /* Function to send mail to the client

  @param stat: Log stats object - If Error, Has no of errors, Application ID, Time start and time end
  */
  def sendEmail(stat: LogStats) = {
    //If error exists
    if(stat.flagErrors == 1){
      //Setting the email content
      val from = conf.getString("email.emailId")
      val to = conf.getString("email.emailId")
      val HTMLBODY = conf.getString("email.bodyTemplate").format(stat.numErrors, stat.numLogs, stat.app, stat.windowstart, stat.windowend)
      val SUBJECT = conf.getString("email.subject")
      // Creating Email client
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
      //Sending request
      client.sendEmail(request)
      "email sent successfully"
    }
  }


  override def shape(): StreamletShape = StreamletShape(in)
  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic {
    override def runnableGraph(): RunnableGraph[_] =
      sourceWithCommittableContext(in).map(stat => sendEmail(stat)).to(committableSink)
  }

}
