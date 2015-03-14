package com.dallaway.telegram.email

import scalax.file.Path
import scalax.io.StandardOpenOption

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.imageio.IIOImage
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter

import scala.collection.JavaConversions._

import org.apache.sanselan.Sanselan
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants
import org.imgscalr.Scalr

object ImageResizer {

  def main(args: Array[String]) {
    if (args.length != 2) println("Usage: ImageResizer in.jpg out.jpg")
    else {
      val (source,dest) = ( Path.fromString(args(0)), Path.fromString(args(1)) )
      println(ImageResizer.scale(source, "image/jpeg", dest, 500))
    }
  }

  // Pimp JpegImageMetadata to add a rotation check
  implicit class RichMeta(meta: JpegImageMetadata) {

    // thank you: http://jpegclub.org/exif_orientation.html
    val exifCodeToAngle = Map(
      6 -> Scalr.Rotation.CW_90,   // turn right
      8 -> Scalr.Rotation.CW_270,  // right left
      3 -> Scalr.Rotation.CW_180 	 // flip
    )

    def checkRotation(in: BufferedImage): Option[BufferedImage] =
      for {
        v     <- Option(meta findEXIFValue TiffTagConstants.TIFF_TAG_ORIENTATION)
        angle <- exifCodeToAngle.get(v.getIntValue)
      } yield Scalr.rotate(in, angle)
  }

  def scale(source: Path, mimeType: String, dest: Path, targetWidth: Int): Option[ImageSize] = {

    val sourceImage = source.inputStream().acquireAndGet(ImageIO.read)

    // Check to see if rotation is required
    val toScale: BufferedImage =
      Sanselan.getMetadata(source.inputStream().byteArray) match {
        case m: JpegImageMetadata => m.checkRotation(sourceImage) getOrElse sourceImage
        case _ => sourceImage
      }

    // Scale and write:
    val img = Scalr.resize(toScale, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, targetWidth, Scalr.OP_BRIGHTER)
    write(img, mimeType, dest)
  }

  private def write(img: BufferedImage, mimeType: String, dest: Path): Option[ImageSize] =
    for ( writer <- ImageIO.getImageWritersByMIMEType(mimeType).toList.headOption) yield {
      dest.outputStream(StandardOpenOption.Create).acquireFor { out =>
        val ios = ImageIO.createImageOutputStream(out)
        writer.setOutput(ios)
        writer.write(
          /*metadata=*/null,
          new IIOImage(img, /*thumbnails=*/null, /*imageMetaData=*/null),
          /*params=*/bestFor(writer))
        writer.dispose()
        ios.close()
      }

      ImageSize(img.getWidth, img.getHeight)
    }

  private def bestFor(writer: ImageWriter): ImageWriteParam = {
    val params = writer.getDefaultWriteParam

    if (params.canWriteProgressive)
      params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT)

    if (params.canWriteCompressed) {
      // Not all image formats support compression/quality settings:
      params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
      params.setCompressionQuality(1)
    }

    params
  }

}