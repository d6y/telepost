package com.dallaway.telegram.email

import scala.xml.NodeSeq

case class ImapCredentials(username: String, password: String, host: String = "imap.gmail.com")

case class EmailMeta(
  sender:   String,
  sentDate: java.util.Date,
  title:    String
) {
  lazy val slug = Hoisted.slugify(title)
}

case class EmailInfo(
  meta:        EmailMeta,
  body:        String,
  attachments: Seq[ImageAttachment]
)

case class ImageSize(width: Int, height: Int)

case class ImageAttachment(
  fullUrlPath:   String,
  inlineUrlPath: String,
  inlineSize:    ImageSize,
  mineType:      String
) {
  def toHtml =
    <div>
        <a href={fullUrlPath}>
          <img src    = {inlineUrlPath}
               width  = {inlineSize.width.toString}
               height = {inlineSize.height.toString} />
        </a>
      </div>
}
