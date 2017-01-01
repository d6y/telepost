package com.dallaway.telegram.email

import scalax.file.Path
import java.text.SimpleDateFormat

trait BlogWriter {

  type BlogFilename = String

  def blog(postsDir: Path)(email: EmailInfo): BlogFilename = {

    val attachments = email.attachments.map(_.toHtml).mkString

    val featured = email.attachments.headOption.map(_.inlineUrlPath).map(url =>
      s"image: $url"
    )

    val dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(email.meta.sentDate)

    val blog =
      """|---
         |title: |
         |  %s
         |author: %s
         |date: %s
         |layout: post
         |comments: true
         |%s
         |---
         |
         |%s
         |
         |%s
      """.stripMargin.format (
        email.meta.title,
        email.meta.sender,
        dateTime,
        featured getOrElse "",
        attachments,
        email.body)

    val date = new SimpleDateFormat("yyyy-MM-dd").format(email.meta.sentDate)
    val filename = date + "-" + email.meta.slug + ".md"

    (postsDir / filename).write(blog)

    filename
  }
}
