package com.dallaway.telegram.email

import javax.mail._

trait Imap {

  // Apply handler to each email found, returning the number of emails found
  def checkMail(login: ImapCredentials)(handler: javax.mail.Message => Unit): Int = {

    def withInbox[T](f: javax.mail.Folder ⇒ T) : T = {
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

      val n = inbox.getMessageCount

      if (n > 0)
        for(i <- 1 to n) {
          val m = inbox.getMessage(i)
          try {
            handler(m)
            m.setFlag(Flags.Flag.DELETED, true) // archive successfully processed messages
          } catch {
            case x : Throwable => x.printStackTrace()
          }
        }

       n
    }

  }

}
