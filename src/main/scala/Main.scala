import scala.util.{Failure, Success, Try}
import scalax.file.Path

import com.dallaway.telegram.email._

object Main extends Imap with EmailWriter with BlogWriter {

  System.setProperty("java.awt.headless", "true")

  def temporaryDir: Path = {
    import java.nio.file.Files
    val mediadir = Files.createTempDirectory("telepost")
    val path     = Path(mediadir.toFile)

    Runtime
      .getRuntime()
      .addShutdownHook(new Thread() {
        override def run(): Unit = {
          val _ = path.deleteRecursively()
        }
      })

    path
  }

  def main(args: Array[String]): Unit = args match {
    case Array(posts, email, password, bucket, s3key, s3secret) =>
      val emailLogin    = ImapCredentials(email, password)
      val s3credentials = S3.credentials(s3key, s3secret)
      val mediadir      = temporaryDir
      val postsdir =
        Path.fromString(posts).createDirectory(failIfExists = false)

      val save   = write(mediadir) _
      val mkblog = blog(postsdir) _
      val s3     = S3(bucket, s3credentials, mediadir)

      val extractContent = (save andThen s3.putAttachments)

      def succeed(msg: String): Unit = {
        println(msg)
        System.exit(0)
      }

      def fail(ex: Throwable): Unit = {
        println(ex.getMessage)
        System.exit(1)
      }

      val result = checkMail(emailLogin) { email =>
        extractContent(email).map(mkblog).recoverWith {
          // add information about the email into the failure
          case err =>
            Failure[BlogFilename](new RuntimeException(s"Failure processing ${email.getSubject}", err))
        }
      }

      result match {
        case NothingToDo              => succeed("Nothing to do")
        case Fail(cause)              => fail(cause)
        case ConnectionTimeout(cause) => succeed(s"Timeout: $cause")
        case Processed(posts) =>
          val happy = posts.collect { case Success(file) => file }
          val sad   = posts.collect { case Failure(err)  => err }

          happy.foreach { file =>
            println(s"Processed: $file")
          }
          sad.foreach { err =>
            err.printStackTrace
          }

          if (sad.isEmpty) succeed("Posts processed") else fail(sad.head)
      }

    case _ =>
      println("Usage: Main posts-dir email password bucket s3-key s3-secret")
  }

}
