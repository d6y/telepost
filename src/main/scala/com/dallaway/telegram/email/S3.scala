package com.dallaway.telegram.email

import scala.util.Try
import java.io.File
import scalax.file.Path

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{PutObjectRequest, CannedAccessControlList}

object S3 {
  def credentials(key: String, secret: String): AWSCredentials =
    new BasicAWSCredentials(key, secret)
}

// We store images on S3.
// An assumption here is that the URL is the same as the bucket name.
case class S3(bucketName: String, credentials: AWSCredentials, mediaDir: Path) {

  // Write the attachments (full and thumbnail) to S3.
  // Return an EmailInfo value with the attachments updated to their URL on S3.
  def putAttachments(email: EmailInfo): Try[EmailInfo] = Try {

    val client = new AmazonS3Client(credentials)

    def put(filename: String): Unit = {
      val file = new File((mediaDir / filename).toAbsolute.path)
      val req = new PutObjectRequest(bucketName, filename, file).withCannedAcl(CannedAccessControlList.PublicRead)
      client.putObject(req)
    }

    val s3attachments = for {
      a <- email.attachments
      _  = put(a.fullUrlPath)
      _  = put(a.inlineUrlPath)
    } yield a.copy(
        fullUrlPath   = s"http://$bucketName/${a.fullUrlPath}",
        inlineUrlPath = s"http://$bucketName/${a.inlineUrlPath}"
      )

    email.copy(attachments = s3attachments)
  }

}
