package com.dallaway.telegram.email

import javax.mail._
import scala.util.Try

trait Imap {

  // Apply handler to each email found, returning the number of emails found
  def checkMail[R](login: ImapCredentials)(handler: javax.mail.Message => Try[R]): Seq[Try[R]] = {

    def withInbox[T](f: javax.mail.Folder => T) : T = {
      val props = new java.util.Properties
      props.put("mail.store.protocol", "imaps")

      val session = Session.getDefaultInstance(props)
      session.setDebug(false)

      val store = session.getStore
      store.connect(login.host, login.username, login.password)

      val inbox = store.getFolder("INBOX")
      inbox.open(Folder.READ_WRITE)

      val result = f(inbox)

      inbox.close(/*expurge=*/true)
      store.close()

      result
    }


    withInbox { inbox =>
      inbox.getMessageCount match {
        case 0 => Seq.empty
        case n => for {
          i      <- 1 to n
          m      =  inbox.getMessage(i)
          result =  handler(m)
          _      =  m.setFlag(Flags.Flag.DELETED, true) // archive
        } yield result
      }
    }

  }

}
