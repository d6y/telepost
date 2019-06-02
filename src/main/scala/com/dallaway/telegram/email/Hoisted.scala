package com.dallaway.telegram.email

object Hoisted {

  // from hoisted
  def slugify(in: String): String = {
    val safe           = """[^\w]""".r
    val r1             = safe.replaceAllIn(in.trim.toLowerCase, "-")
    val noLeadingDash  = """^(\-)+""".r
    val notrailingDash = """(\-)+$""".r
    val r2             = noLeadingDash.replaceAllIn(r1, "")
    notrailingDash.replaceAllIn(r2, "") match {
      case "" => "x"
      case s  => s
    }
  }

}
