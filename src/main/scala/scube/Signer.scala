package scube

import java.util.Date
import scala.collection.SortedMap
import java.io.InputStream

object Signer {

  def apply(credentials:Credentials,
            bucket:Option[Bucket],
            method:String,
            path:String,
            contentType:Option[String],
            content:Option[InputStream],
            headers:S3.Headers):S3.Headers = {

    val date = RFC822(new Date)
    val contentMd5 = content.map(ContentMD5.apply)
    val sessionToken = credentials.sessionToken
    val resourcePath = bucket.fold(path)(b => s"/$b${path}")

    val sortedHeaders = SortedMap(headers.toSeq ++
      Seq(
        Some("Date" -> Seq(date)),
        Some("Host" -> Seq(host(bucket))),
        contentType.map("Content-Type" -> Seq(_)),
        contentMd5.map("Content-Md5" -> Seq(_)),
        sessionToken.map("X-Amz-Security-Token" -> Seq(_))
      ).collect { case Some(pair) => pair }:_*)

    val signature = Signature(credentials.secretKey, s"""
      |$method
      |${contentMd5.getOrElse("")}
      |${contentType.getOrElse("")}
      |${date}
      |${formatHeaders(sortedHeaders)}$resourcePath
    """.stripMargin.trim)

    sortedHeaders.toMap + ("Authorization" -> Seq(s"AWS ${credentials.accessKeyId}:$signature"))
  }

  def host(bucket:Option[Bucket]):String = bucket.fold("s3.amazonaws.com")(bucket => s"$bucket.s3.amazonaws.com")

  private def formatHeaders(headers:SortedMap[String,Seq[String]]):String = {
    headers.filter(_._1.toLowerCase.startsWith("x-amz")).map {
      case (header,values) => s"${header.toLowerCase}:${values.mkString(",")}\n"
    }.mkString
  }
}
