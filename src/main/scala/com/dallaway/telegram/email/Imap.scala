package com.dallaway.telegram.email

import javax.mail._
import scala.util.Try

sealed trait Result
case class ConnectionTimeout(ex: Throwable) extends Result
case object NothingToDo extends Result
case class Fail(ex:            Throwable) extends Result
case class Processed[R](posts: Seq[Try[R]]) extends Result

trait Imap {

  // Apply handler to each email found, returning the number of emails found
  def checkMail[R](login: ImapCredentials)(handler: javax.mail.Message => Try[R]): Result = {

    def withInbox[T](f: javax.mail.Folder => T): T = {
      val props = new java.util.Properties
      props.put("mail.store.protocol", "imaps")

      val session = Session.getDefaultInstance(props)
      session.setDebug(false)

      val store = session.getStore
      store.connect(login.host, login.username, login.password)

      val inbox = store.getFolder("INBOX")
      inbox.open(Folder.READ_WRITE)

      val result = f(inbox)

      inbox.close( /*expurge=*/ true)
      store.close()

      result
    }

    val processMailFolder: javax.mail.Folder => Result = inbox =>
      inbox.getMessageCount match {
        case 0 => NothingToDo
        case n =>
          Processed(for {
            i <- 1 to n
            m      = inbox.getMessage(i)
            result = handler(m)
            _      = m.setFlag(Flags.Flag.DELETED, true) // archive
          } yield result)
    }

    def timeout(mx: javax.mail.MessagingException): Boolean =
      mx.getCause match {
        case cx: java.net.ConnectException if cx.getMessage contains "timed out" =>
          true
        case _ => false
      }

    try {
      withInbox { processMailFolder }
    } catch {
      case mx: javax.mail.MessagingException if timeout(mx) =>
        ConnectionTimeout(mx)
      case ex: Throwable => Fail(ex)
    }

  }

}
