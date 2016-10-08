
import scalax.file.Path

import com.dallaway.telegram.email._

object Main extends Imap with EmailWriter with BlogWriter {

  System.setProperty("java.awt.headless", "true")

  def temporaryDir: Path = {
    import java.nio.file.Files
    val mediadir = Files.createTempDirectory("telepost")
    mediadir.toFile.deleteOnExit()
    Path(mediadir.toFile)
  }

  def main(args: Array[String]): Unit = args match {
    case Array(posts, email, password, bucket, s3key, s3secret) =>

      val emailLogin = ImapCredentials(email, password)
      val mediadir = temporaryDir
      val postsdir = Path.fromString(posts).createDirectory(failIfExists=false)

      val save = write(mediadir) _
      val mkblog = blog(postsdir) _

      val telegram = save andThen mkblog

      val numEmails = checkMail(emailLogin) { email => telegram(email) }

      // By convention, exit codes of zero indicate success, but we're
      // returning the number of messages seen.
      // So zero would mean "did nothing", and 1 would mean "saw an email".
      System.exit(numEmails)

    case _ => println("Usage: Main postsDir tempDir email emailPassword bucket s3-key s3-secret")
  }

}
