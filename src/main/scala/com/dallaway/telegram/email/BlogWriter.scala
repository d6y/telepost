package com.dallaway.telegram.email

import scalax.file.Path
import java.text.SimpleDateFormat

trait BlogWriter {

  type BlogFilename = String

  def blog(postsDir: Path)(email: EmailInfo): BlogFilename = {

    val attachments = email.attachments.map(_.toHtml).mkString

    val dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(email.sentDate)
    val date = new SimpleDateFormat("yyyy-MM-dd").format(email.sentDate)

    val blog =
      """|---
         |title: %s
         |author: %s
         |date: %s
         |layout: post
         |comments: true
         |---
         |
         |%s
         |
         |%s
      """.stripMargin.format (
        email.title, email.sender, dateTime, attachments, email.body)

    val filename = date + "-" + Hoisted.slugify(email.title) + ".md"

    (postsDir / filename).write(blog)

    filename
  }
}