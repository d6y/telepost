package com.dallaway.telegram.email

// Given an email `Message`, this trait will:
//
// - extract the sender, date and text.
//
// - save image attachments, and produce thumbnail versions.

import scalax.io._
import Resource._
import scalax.file.Path
import javax.mail._
import javax.mail.internet._
import java.io.InputStream

import scala.language.postfixOps

trait EmailWriter {

  // **Entry point**:
  // Writes out the image attachments found in an email to the file system.
  // Returns the details about the email content and  where to find the attachments.
  def write(mediaDir: Path)(m: Message): EmailInfo = {

    val bodyText = body(m)

    val meta = EmailMeta(
      sender(m),
      m.getSentDate,
      Option(m.getSubject) getOrElse bodyText
    )

    EmailInfo(meta, bodyText, for (a <- attachments(mediaDir, m, meta)) yield a)
  }

  // The sender (ideally, their real name).
  private def sender(m: javax.mail.Message): String = m.getFrom().head match {
    case a: InternetAddress => a.getPersonal()
    case a => a.toString()
  }

  // All the parts of the message.
  implicit class MultipartHelper(m: MimeMultipart) {
    def bodyParts = for (i <- 0 until m.getCount) yield m.getBodyPart(i)
  }

  // Turns "IMAGE/JPEG; name=1557861979_af07a31642.jpg" into "image/jpg".
  implicit class BodyHelper(b: Part) {
    def mimeType = b.getContentType.split(";").head.toLowerCase
  }

  // The body text of a message.
  private def body(m: Part): String = m.getContent match {
    case c: String if m.isMimeType("text/plain") => c
    case p: MimeMultipart                        => p.bodyParts map { body } mkString " "
    case _ => ""
  }

  // Locate and extract each attachment in the email:
  private def attachments(mediaDir: Path, m: Part, meta: EmailMeta): Seq[ImageAttachment] =
    m.getContent match {

      // * A body part attachment
      case p: MimeMultipart =>
        for {
          i <- 0 until p.getCount
          b = p.getBodyPart(i)
          if b.getDisposition != null
          uniqueName = s"$i-${b.getFileName}"
          a <- savedAttachment(mediaDir, b.getInputStream, b.mimeType, Clean(uniqueName, meta))
        } yield a

      // * Inline content
      case s: InputStream =>
        savedAttachment(mediaDir, s, m.mimeType, Clean(m.getFileName, meta)).toList

      // * Content we don't need to handle
      case otherwise => println("Found a " + otherwise); Nil
    }

  // Save one attachment to disk at full-size, and one scaled to width of 500px.
  private def savedAttachment(
    mediaDir: Path,
    in:       => InputStream,
    mimeType: String,
    fileName: Clean
  ): Option[ImageAttachment] = {

    // - Save original image (to link to):
    val dest = mediaDir / fileName.fullName
    dest.write(fromInputStream(in).bytes)

    // - Scaled version to show inline the blog:
    val width      = 500
    val inlineFile = mediaDir / fileName.thumbName

    for (inlineSize <- ImageResizer.scale(dest, mimeType, inlineFile, width))
      yield ImageAttachment(dest.name, inlineFile.name, inlineSize, mimeType)
  }

}
