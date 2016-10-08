package com.dallaway.telegram.email

import java.text.SimpleDateFormat

object Clean {
  // Construct a cleaned-up attachement filename.
  // E.g., 2016-10-08-hello-world-fullsize-IMG_266.jpg
  def apply(raw: String, meta: EmailMeta): Clean = {
    val name = raw.replace(' ', '_').replaceAllButLast('.', '_')
    val date = new SimpleDateFormat("yyyy-MM-dd").format(meta.sentDate)
    Clean(s"$date-${meta.slug}-fullsize-$name")
  }

  implicit class StringHelper(in: String) {
    def replaceAllButLast(source: Char, dest: Char): String =
      in.lastIndexOf(source) match {
        case -1 => in
        case n => in.substring(0,n).replace(source,dest) + in.substring(n)
      }
  }
}

case class Clean(fullName: String) {
  // Convention for the thumbnail filename
  def thumbName: String = fullName.replace("-fullsize-", "-thumb-")
}

