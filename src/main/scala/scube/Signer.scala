package scube

import java.util.Date

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

    val newHeaders:S3RequestBuilder.Headers = headers +
      Pair("Date", Seq(date)) +
      Pair("Host", Seq(host)) ++
      Seq(
        contentType.map("Content-Type" -> Seq(_)),
        contentMd5.map("Content-Md5" -> Seq(_)),
        sessionToken.map("X-Amz-Security-Token" -> Seq(_))
      ).collect { case Some(pair) => pair }

    val sortedHeaders = sortAndFilterHeaders(newHeaders)

    s"""
      |$method
      |${contentMd5.getOrElse("")}
      |${contentType.getOrElse("")}
      |${date}
      |${sortedHeaders}$path
    """.stripMargin.trim

    headers + ("Authorization" -> Seq(s"AWS ${credentials.accessKeyId}:j11vSh0A3tMeTbcYrfa55J+d2h0="))
  }

  private def sortAndFilterHeaders(headers:S3RequestBuilder.Headers):S3RequestBuilder.Headers = {
    val sortedHeaders = headers.keys.filter(_.toLowerCase.startsWith("x-amz")).toSeq.sorted
    sortedHeaders
    Map.empty
  }

  // headers: Map[String, util.Collection[String]]
  // inputStream for contentMd5

//
//      |$method
//      |${contentMd5.getOrElse("")}
//      |${contentType.getOrElse("")}
//      |$dateTime
//      |${sortedHeaders.map(k => k.toLowerCase + ":" + headers(k).mkString(",") + "\n").mkString}$resourcePath
//    """.stripMargin.trim
}
