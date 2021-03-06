package com.dallaway.telegram.email

import javax.imageio.ImageIO
import scalax.file.Path

import scala.util.Try

object ImageResizer {

  def main(args: Array[String]) {
    if (args.length != 2) println("Usage: ImageResizer in.jpg out.jpg")
    else {
      val (source, dest) = (Path.fromString(args(0)), Path.fromString(args(1)))
      println(ImageResizer.scale(source, "image/jpeg", dest, 500))
    }
  }

  def scale(source: Path, mimeType: String, dest: Path, width: Int): Option[ImageSize] = {
    val attempt = for {
      _    <- magicScale(source, width, dest)
      size <- readImageSize(dest)
    } yield size

    attempt match {
      case scala.util.Success(size) => Some(size)
      case err =>
        Console.err.println(s"Scale failed: $err")
        None
    }
  }

  def readImageSize(path: Path): Try[ImageSize] = Try {
    val img = ImageIO.read(path.toURL)
    ImageSize(img.getWidth, img.getHeight)
  }

  // Java API version of:
  // convert in.jpg -resize 500x -auto-orient out.jpg
  def magicScale(source: Path, width: Int, dest: Path): Try[Unit] = Try {
    import org.im4java.core._
    val cmd = new ConvertCmd()
    val op  = new IMOperation()
    source.fileOption.foreach(file => op.addImage(file.getAbsolutePath))
    op.resize(width, null)
    op.autoOrient()
    dest.fileOption.foreach(file => op.addImage(file.getAbsolutePath))
    cmd.run(op)
  }

}
