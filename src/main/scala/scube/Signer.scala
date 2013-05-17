package scube

import java.util.Date
import scala.collection.mutable.LinkedHashMap
import scala.collection.SortedMap

object Signer {

  def apply(bucket:Bucket,
            method:String,
            path:String,
            headers:S3RequestBuilder.Headers):S3RequestBuilder.Headers = {
    sign(bucket.credentials, Some(bucket), method, path, None, None, headers)
  }

  def apply(credentials:Credentials,
             method:String,
             path:String,
             headers:S3RequestBuilder.Headers):S3RequestBuilder.Headers = {
    sign(credentials, None, method, path, None, None, headers)
  }

  def sign(credentials:Credentials,
            bucket:Option[Bucket],
            method:String,
            path:String,
            contentType:Option[String],
            content:Option[Array[Byte]],
            headers:S3RequestBuilder.Headers):S3RequestBuilder.Headers = {

    val date = RFC822(new Date)
    val host = bucket.fold("s3.amazonaws.com")(bucket => s"$bucket.s3.amazonaws.com")
    val contentMd5 = content.map(ContentMD5.apply)
    val sessionToken = credentials.sessionToken

    val sortedHeaders = SortedMap(headers.toSeq ++
      Seq(
        Some("Date" -> Seq(date)),
        Some("Host" -> Seq(host)),
        contentType.map("Content-Type" -> Seq(_)),
        contentMd5.map("Content-Md5" -> Seq(_)),
        sessionToken.map("X-Amz-Security-Token" -> Seq(_))
      ).collect { case Some(pair) => pair }:_*)

    // Verify resourcePath is correct given the bucket...

    val signature = Signature(credentials.accessKeyId, s"""
      |$method
      |${contentMd5.getOrElse("")}
      |${contentType.getOrElse("")}
      |${date}
      |${formatHeaders(sortedHeaders)}$path
    """.stripMargin.trim)

    sortedHeaders.toMap + ("Authorization" -> Seq(s"AWS ${credentials.accessKeyId}:$signature"))
  }

  private def formatHeaders(headers:SortedMap[String,Seq[String]]):String = {
    headers.filter(_._1.toLowerCase.startsWith("x-amz")).map {
      case (header,values) => s"$header:${values.mkString(",")}"
    }.mkString("\n")
  }
}
