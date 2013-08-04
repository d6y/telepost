package com.dallaway.telegram.email

// Given an email `Message`, this trait will:
//
// - extract the sender, date and text.
//
// - save image attachments, and produce thumbnail versions.

import scalax.io._
import Resource._
import scalax.file.Path
import scalax.file.PathMatcher._
import javax.mail._
import javax.mail.internet._
import java.io.InputStream

import scala.language.postfixOps

trait EmailWriter {

  // **Entry point**:
  // Writes out the image attachments found in an email to the file system.
  // Returns the details about the email content and  where to find the attachments.
  def write(mediaDir: Path)(m: Message): EmailInfo =
    EmailInfo(
      sender(m),
      Option(m.getSubject),
      body(m),
      m.getSentDate,
      for ( a <- attachments(mediaDir, m) ) yield a
    )


  // Prevent over-writing of a file by ensuring a Path is unique.
  implicit class PathHelper(path: Path) {
    def ensureUnique = uniquely(path)
    private def randomChar = scala.util.Random.shuffle('A' to 'Z' toSeq).head
    private def uniquely(path: Path): Path = path match {
      case Exists(p) => uniquely { Path(p.segments.init:_*) / (randomChar + p.name) }
      case _ => path
    }
  }

  // The sender (ideally, their real name).
  private def sender(m: javax.mail.Message): String = m.getFrom().head match {
    case a: InternetAddress => a.getPersonal
    case a => a.toString
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
    case p: MimeMultipart => p.bodyParts map { body } mkString " "
    case _ => ""
  }

  // Locate and extract each attachment in the email:
  private def attachments(mediaDir: Path, m: Part) : Seq[Attachment] = m.getContent match {

    // * A body part attachment
    case p: MimeMultipart => for {
      i <- 0 until p.getCount
      b = p.getBodyPart(i)
      if b.getDisposition != null
      a <- savedAttachment(mediaDir, b.getInputStream, b.mimeType, b.getFileName)
    } yield a


    // * Inline content
    case s: InputStream => savedAttachment(mediaDir, s, m.mimeType, m.getFileName).toList

    // * Content we don't need to handle
    case otherwise => println("Found a "+otherwise); Nil
  }

  // Utility to replace all but the lass occurrence of a character. E.g., for "2013.10.12.jpg" -> "2013_10_12.jpg"
  implicit class StringHelper(in: String) {
    def replaceAllButLast(source: Char, dest: Char) : String = {
      (in lastIndexOf source) match {
        case -1 => in
        case n => in.substring(0,n).replace(source,dest) + in.substring(n)
      }
    }
  }

  // Remove unhelpful characters from the attachment filename.
  private def cleanName(in: String) = "tp_" + in.replace(' ', '_').replaceAllButLast('.', '_')

  // Save one attachment to disk at full-size, and one scaled to width of 500px.
  private def savedAttachment(mediaDir: Path, in: =>InputStream, mimeType: String, fileName: String) : Option[Attachment] = {

    // - Save original image (to link to):
    val dest = (mediaDir / cleanName(fileName)).ensureUnique
    dest.write(fromInputStream(in).bytes)

    // - Scaled version to show inline the blog:
    val width = 500
    val inlineImgName = dest.name + width.toString + dest.name.split("\\.").last
    val inlineFile = mediaDir / inlineImgName

      for ( inlineSize <- ImageResizer.scale(dest, mimeType, inlineFile, width) )
      yield
        ImageAttachment("/media/"+dest.name, "/media/"+inlineImgName, inlineSize, mimeType)

  }


}