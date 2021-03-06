package com.dallaway.telegram.email

import scala.util.Try
import java.io.File
import scalax.file.Path

import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import com.amazonaws.regions.Regions.US_EAST_1

object S3 {
  def credentials(key: String, secret: String): AWSCredentials =
    new BasicAWSCredentials(key, secret)
}

// We store images on S3.
// An assumption here is that the URL is the same as the bucket name.
case class S3(bucketName: String, credentials: AWSCredentials, mediaDir: Path) {

  // Write the attachments (full and thumbnail) to S3.
  def putAttachments(email: EmailInfo): Try[EmailInfo] = Try {

    val client = AmazonS3ClientBuilder
      .standard()
      .withRegion(US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build()

    def put(filename: String): Unit = {
      val file = new File((mediaDir / filename).toAbsolute.path)
      val req = new PutObjectRequest(bucketName, filename, file)
        .withCannedAcl(CannedAccessControlList.PublicRead)
      val objectInfo = client.putObject(req)
    }

    val s3attachments = for {
      a <- email.attachments
      _ = put(a.fullUrlPath)
      _ = put(a.inlineUrlPath)
    } yield
      a.copy(
        fullUrlPath   = s"http://$bucketName/${a.fullUrlPath}",
        inlineUrlPath = s"http://$bucketName/${a.inlineUrlPath}"
      )

    email.copy(attachments = s3attachments)
  }

}
