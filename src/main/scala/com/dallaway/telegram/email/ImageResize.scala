package com.dallaway.telegram.email

import scalax.file.Path
import java.awt.image.BufferedImage
import java.awt.Toolkit
import javax.swing.ImageIcon
import java.awt.geom.AffineTransform
import javax.imageio.ImageIO
import scala.collection.JavaConversions._
import java.awt.image.AffineTransformOp
import scalax.io.Resource
import java.io.OutputStream
import javax.imageio.IIOImage
import scalax.io.OpenOption
import scalax.io.StandardOpenOption
import org.apache.sanselan.Sanselan
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants
import org.apache.sanselan.formats.tiff.TiffField
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter

object ImageResizer {

  def main(args: Array[String]) {
    if (args.length != 2) println("Usage: ImageResizer in.jpg out.jpg")
    else {
      val (source,dest) = ( Path.fromString(args(0)), Path.fromString(args(1)) )
      println(ImageResizer.scale(source, "image/jpeg", dest, 500))
    }
  }
  
  implicit def affineHelper(xform: AffineTransform) = new {

    // thank you: http://jpegclub.org/exif_orientation.html
    val exifCodeToAngle = Map(
        6 → 90,   // turn right
        8 → 270,  // right left
        3 → 180 	// flip
    )

    def correctOrientation(size: ImageSize, f: TiffField) = for (angle ← exifCodeToAngle.get(f.getIntValue) ) {
        val midx = size.width.toDouble / 2d
        val midy = size.height.toDouble / 2d

        if (angle == 90 || angle == 270) {
           val dx: Double = midx - midy
           xform.translate(-dx, dx)
        }

        xform.rotate(math.toRadians(angle), midx, midy)
    }
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

  def scale(source: Path, mimeType: String, dest: Path, targetWidth: Int): Option[ImageSize] = {

     val sourceImage = source.inputStream.acquireAndGet(ImageIO.read)
     val size = ImageSize(sourceImage.getWidth(null), sourceImage.getHeight(null))
     val scale: Double = targetWidth.toDouble / size.width.toDouble

     val xform = new AffineTransform
     xform.scale(scale, scale)

     // Check to see if rotation is required
     Sanselan.getMetadata(source.inputStream.byteArray) match {
       case m: JpegImageMetadata ⇒ for ( v ← Option(m.findEXIFValue(TiffTagConstants.TIFF_TAG_ORIENTATION)) ) {
         xform.correctOrientation(size, v)
       }
       case _ ⇒
     }
     
     // Write:
     for (
         writer ← ImageIO.getImageWritersByMIMEType(mimeType).toList.headOption
     ) yield {
       val op = new AffineTransformOp(xform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
       val img = op.filter(sourceImage, null /*null means create the image for us*/)

       dest.outputStream(StandardOpenOption.Create).acquireFor { out ⇒
         val ios = ImageIO.createImageOutputStream(out)
         writer.setOutput(ios)
         writer.write(
             /*metadata=*/null,
             new IIOImage(img, /*thumbnails=*/null, /*imageMetaData=*/null),
             /*params=*/bestFor(writer))
         writer.dispose
         ios.close
       }

       ImageSize(img.getWidth, img.getHeight)

     }


  }

 
  


}

