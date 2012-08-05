package com.dallaway.telegram.email

import scalax.io._
import Resource._
import scalax.file.Path
import scalax.file.PathMatcher._

import javax.mail._
import javax.mail.internet._
import com.sun.mail.imap._


trait EmailWriter {

  /** Writes attachments, returning the details about the email content and
   * where to find the attachments on the file system.  */
	def write(mediaDir: Path)(m: Message): EmailInfo = 
	  EmailInfo(
			sender(m),
			m.getSubject,
			body(m),
			m.getSentDate,
			for ( a ← attachments(m); sa ← savedAttachment(mediaDir, a) ) yield sa )


  implicit def pathHelper(path: Path) = new {
	  /**  Prevent over-writing of a file by ensuring a Path is unique */
	  def ensureUnique = uniquely(path)
		private def randomChar = scala.util.Random.shuffle('A' to 'Z' toSeq).head
	  private def uniquely(path: Path): Path = path match {
		  case Exists(p) ⇒ uniquely { Path(p.segments.init:_*) / (randomChar + p.name) }
		  case _ ⇒ path
		}
	}
		
	private def sender(m: javax.mail.Message): String = m.getFrom().head match {
	  case a: InternetAddress ⇒ a.getPersonal
		case a => a.toString
	}

	implicit def multipartHelper(m: MimeMultipart) = new {
		def bodyParts = for (i <- 0 until m.getCount) yield m.getBodyPart(i)
	}

	// "IMAGE/JPEG; name=1557861979_af07a31642.jpg" ⇒ "image/jpg"
	implicit def bodyHelper(b: BodyPart) = new {
		def mimeType = b.getContentType.split(";").head.toLowerCase
	}    

	private def body(m: Part): String = m.getContent match {
		case c: String if m.isMimeType("text/plain") ⇒ c
		case p: MimeMultipart ⇒ p.bodyParts map { body } mkString " "
		case _ ⇒ ""
	}

	private def attachments(m: Part) : Seq[BodyPart] = m.getContent match {
		case p: MimeMultipart => for {
			i ← 0 until p.getCount
			b = p.getBodyPart(i)
			if b.getDisposition != null
		} yield b
		case _ ⇒ Nil 
	}

	private def savedAttachment(mediaDir: Path, b: BodyPart) : Option[Attachment] = {
		// Save original image to link to:
		val dest = (mediaDir / b.getFileName).ensureUnique
		dest.write(fromInputStream(b.getInputStream).bytes)

		// Scaled version to show in the blog:
		val width = 500
		val inlineImgName = dest.name + "."+width+"." + dest.name.split("\\.").last
		val inlineFile = mediaDir / inlineImgName

		for ( inlineSize ← ImageResizer.scale(dest, b.mimeType, inlineFile, width) ) 
		  yield 
		  	ImageAttachment("/media/"+b.getFileName, "/media/"+inlineImgName, inlineSize, b.mimeType)

	}


}