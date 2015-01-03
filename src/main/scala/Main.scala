
import scalax.file.Path

import com.dallaway.telegram.email._

object Main extends App with Imap with EmailWriter with BlogWriter {

  System.setProperty("java.awt.headless", "true")

  require(args.length == 3, "Usage: /path/to/blog email@address.com ema1lp@ssw0rd")

  val blog = Path.fromString(args(0))
  val emailLogin = Credentials(args(1), args(2))

  val mediadir = (blog / "media").createDirectory(failIfExists=false)
  val postsdir = (blog / "_posts").createDirectory(failIfExists=false)

  val save = write(mediadir) _
  val mkblog = blog(postsdir) _

  val telegram = save andThen mkblog

  val numEmails = checkMail(emailLogin) { email => telegram(email) }

  // By convention, exit codes of zero indicate success, but we're
  // returning the number of messages seen. So zero would mean "did nothing", and
  // 1 would mean "saw an email".
  System.exit(numEmails)
}