package com.dallaway.telegram.email

import javax.mail._

trait Imap {

  def checkMail(login: Credentials)(handler: javax.mail.Message ⇒ Unit): Unit = {

    withInbox { inbox ⇒

      val n = inbox.getMessageCount

      if (n > 0)
        for(i ← 1 to n) {
          val m = inbox.getMessage(i)
          try {
            handler(m)
            m.setFlag(Flags.Flag.DELETED, true) // archive successfully processed messages
          } catch {
            case x ⇒ x.printStackTrace
          }
        }

    }


    def withInbox(f: javax.mail.Folder ⇒ Unit) : Unit = {
      val props = new java.util.Properties
      props.put("mail.store.protocol", "imaps")

      val session = Session.getDefaultInstance(props)
      session.setDebug(false)

      val store = session.getStore()
      store.connect(login.host, login.username, login.password)

      val inbox = store.getFolder("INBOX")
      inbox.open(Folder.READ_WRITE)

      f(inbox)

      inbox.close(/*expurge=*/true)
      store.close
  }


  }

}