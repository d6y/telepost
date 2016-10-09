package com.dallaway.telegram.email

import scalax.file.Path
import net.coobird.thumbnailator.Thumbnails

object ImageResizer {

  def main(args: Array[String]) {
    if (args.length != 2) println("Usage: ImageResizer in.jpg out.jpg")
    else {
      val (source,dest) = ( Path.fromString(args(0)), Path.fromString(args(1)) )
      println(ImageResizer.scale(source, "image/jpeg", dest, 500))
    }
  }

  def scale(source: Path, mimeType: String, dest: Path, targetWidth: Int): Option[ImageSize] =
    for {
      in    <- source.fileOption
      thumb =  Thumbnails.of(in).useExifOrientation(true).width(targetWidth).asBufferedImage()
      out   <- dest.fileOption
      _     =  Thumbnails.of(thumb).scale(1).outputQuality(0.85).toFile(out)
    } yield ImageSize(thumb.getWidth, thumb.getHeight)

}
