
import scala.util.{Success, Failure}
import scalax.file.Path

import com.dallaway.telegram.email._

object Main extends Imap with EmailWriter with BlogWriter {

  System.setProperty("java.awt.headless", "true")

  def temporaryDir: Path = {
    import java.nio.file.Files
    val mediadir = Files.createTempDirectory("telepost")
    val path = Path(mediadir.toFile)

    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run(): Unit = path.deleteRecursively()
    })

    path
  }

  def main(args: Array[String]): Unit = args match {
    case Array(posts, email, password, bucket, s3key, s3secret) =>

      val emailLogin = ImapCredentials(email, password)
      val s3credentials = S3.credentials(s3key, s3secret)
      val mediadir = temporaryDir
      val postsdir = Path.fromString(posts).createDirectory(failIfExists=false)

      val save = write(mediadir) _
      val mkblog = blog(postsdir) _
      val s3 = S3(bucket, s3credentials, mediadir)

      val extractContent = (save andThen s3.putAttachments)

      val numEmails = checkMail(emailLogin) { email => 
        extractContent(email).map(mkblog) match {
          case Success(info) => 
            println(s"Processed: $info")
          case Failure(err)  => 
            println(s"Failure processing ${email.getSubject} into $mediadir")
            err.printStackTrace()
        }
      }

      // By convention, exit codes of zero indicate success, but we're
      // returning the number of messages seen. So zero would mean "did nothing", and 1 would mean "saw an email".
      System.exit(numEmails)

    case _ => println("Usage: Main posts-dir email password bucket s3-key s3-secret")
  }

}
