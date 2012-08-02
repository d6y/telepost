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

object ImageResizer {
  
	def scale(source: Path, mimeType: String, dest: Path, targetWidth: Int): Option[ImageSize] = {

		 val sourceImage = source.inputStream.acquireAndGet(ImageIO.read)
	   val size = ImageSize(sourceImage.getWidth(null), sourceImage.getHeight(null))
	   val scale: Double = targetWidth.toDouble / size.width.toDouble
	   
	   val xform = new AffineTransform
	   xform.scale(scale, scale)
	   
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
	           writer.getDefaultWriteParam)
	       writer.dispose
	       ios.close
	     }
	     
	     ImageSize(img.getWidth(), img.getHeight())
	     
	   }
	   
     
	   
	}
	
  
}

