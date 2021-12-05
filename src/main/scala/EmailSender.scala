import java.net.URI
import java.nio.file.FileSystems
import scala.collection.JavaConverters.*
import scala.collection.immutable.HashMap
import java.util
import java.nio.file
import java.io.IOException
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest


object EmailSender extends App {

  def runMain = {
    val from = "skrish45@uic.edu"

    val to = "skrish45@uic.edu"

    val CONFIGSET = "ConfigSet"
    val HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>" + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>" + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>"
    val TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java."
    val SUBJECT = "Programmatic e-mail"

    val client = AmazonSimpleEmailServiceClientBuilder
      .standard()
      .withRegion(Regions.US_EAST_1).build()
    val request = new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(to))
      .withMessage(new Message().
        withBody(new Body()
          .withHtml(new Content().withCharset("UTF-8")
            .withData(HTMLBODY))
          .withText(new Content()
            .withCharset("UTF-8").withData(TEXTBODY)))
        withSubject (new Content()
        .withCharset("UTF-8").withData(SUBJECT)))
      .withSource(from)
    //      .withConfigurationSetName(CONFIGSET)
    client.sendEmail(request)
  }
}


class EmailSender {

  def send_email() = {
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
  }

}
