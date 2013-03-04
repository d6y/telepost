package com.dallaway.telegram.email

import java.text.SimpleDateFormat
import scalax.file.Path

trait BlogWriter {

  type BlogFilename = String

  def blog(postsDir: Path)(email: EmailInfo): BlogFilename = {

    val attachments = email.atttachments.map(_.toHtml).mkString

    val dash = new SimpleDateFormat("yyyy-MM-dd").format(email.sentDate)

    val blog = """
    |title: %s
    |author: %s
    |date: %s
    |
    |%s
    |
    |%s
    """.stripMargin.format (
        email.subject, email.sender, dash, attachments, email.body)

    val filename = dash + "-" + Hoisted.slugify(email.subject) + ".md"

    (postsDir / filename).write(blog)

    filename

  }



}