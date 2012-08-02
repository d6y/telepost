package com.dallaway.telegram.email

import scala.xml.NodeSeq

case class Credentials(username:String, password:String, host:String = "imap.gmail.com")


case class EmailInfo(
      sender:String, 
      subject:String, 
      body:String, 
      sentDate:java.util.Date,
      atttachments:Seq[Attachment])



case class ImageSize(width: Int, height: Int)      

abstract class Attachment(path: String, mimeType:String) {
  def toHtml: NodeSeq
}

case class ImageAttachment(fullUrlPath:String, inlineUrlPath: String, inlineSize: ImageSize, mineType:String) extends Attachment(fullUrlPath, mineType) {
  def toHtml = 
    <div>
  		<a href={fullUrlPath}>
  			<img src={inlineUrlPath} width={inlineSize.width.toString} height={inlineSize.height.toString} />
  	  </a>
  	</div>

}
    
      