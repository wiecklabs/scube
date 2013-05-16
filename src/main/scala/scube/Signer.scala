package scube

import java.util.Date

object Signer {
  def apply(credentials:Credentials,
            bucket:Bucket,
            method:String,
            path:String,
            headers:S3RequestBuilder.Headers,
            contentType:Option[String]):S3RequestBuilder.Headers = {

    val date = RFC822(new Date)

    headers + ("Authorization" -> Seq(s"AWS ${credentials.accessKeyId}:j11vSh0A3tMeTbcYrfa55J+d2h0="))
  }

  // headers: Map[String, util.Collection[String]]
  // inputStream for contentMd5

//  s"""
//      |$method
//      |${contentMd5.getOrElse("")}
//      |${contentType.getOrElse("")}
//      |$dateTime
//      |${sortedHeaders.map(k => k.toLowerCase + ":" + headers(k).mkString(",") + "\n").mkString}$resourcePath
//    """.stripMargin.trim
}
