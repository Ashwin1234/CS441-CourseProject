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

  val emailSender = new EmailSender
  emailSender.send_email()
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
