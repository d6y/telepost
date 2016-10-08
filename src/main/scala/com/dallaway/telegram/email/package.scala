package com.dallaway.telegram.email

import scala.xml.NodeSeq

case class ImapCredentials(username:String, password:String, host:String = "imap.gmail.com")

case class EmailMeta(
  sender      : String,
  sentDate    : java.util.Date,
  title       : String
) {
  lazy val slug = Hoisted.slugify(title)
}

case class EmailInfo(
  meta        : EmailMeta,
  body        : String,
  attachments : Seq[Attachment]
)

case class ImageSize(width: Int, height: Int)

abstract class Attachment(path: String, mimeType: String) {
  def toHtml: NodeSeq
}

case class ImageAttachment(
  fullUrlPath   : String,
  inlineUrlPath : String,
  inlineSize    : ImageSize,
  mineType      : String
) extends Attachment(fullUrlPath, mineType) {
  def toHtml =
      <div>
        <a href={fullUrlPath}>
          <img src    = {inlineUrlPath}
               width  = {inlineSize.width.toString}
               height = {inlineSize.height.toString} />
        </a>
      </div>
}
