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
import org.apache.sanselan.formats.tiff._

import org.apache.sanselan.common.RationalNumber


// Extracts GPS data from an image to produce a lat/lon location.
object ImageLocation {

  def main(args: Array[String]): Unit = {
    args match {
      case Array(path) =>
        println( location(Path.fromString(path)) )
      case _ =>
      println("Usage: ImageLocation in.jpg")
    }
  }

  case class Location(lat: Float, lon: Float)

  private[this] def toDecimal(degrees: RationalNumber, minutes: RationalNumber, seconds: RationalNumber): Float =
    degrees.floatValue + (minutes.floatValue / 60f) + (seconds.floatValue / 3600f)

  private[this] def sign(ref: String): Int =
    ref.trim match {
      case "W" | "S" =>  -1
      case _         =>  1
    }

  def location(source: Path): Option[Location] = {

    val gps = Sanselan.getMetadata(source.inputStream().byteArray) match {
        case m: JpegImageMetadata => for {
          exif <- Option(m.getExif)
          gps  <- Option(exif.getGPS)
          } yield gps
        case o => None
      }

    gps.map{ g =>
      Location( sign(g.latitudeRef)  * toDecimal(g.latitudeDegrees,  g.latitudeMinutes,  g.latitudeSeconds),
                sign(g.longitudeRef) * toDecimal(g.longitudeDegrees, g.longitudeMinutes, g.longitudeSeconds) )
    }

  }

  // TODO: gecode?
  // url 'https://maps.googleapis.com/maps/api/geocode/json?latlng=50.923923,-0.2747226&key=xxx&location_type=APPROXIMATE&result_type=postal_town'

}