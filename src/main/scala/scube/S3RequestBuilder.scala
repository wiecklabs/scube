package scube

import dispatch._
import com.ning.http.client.{RequestBuilder, Request, FluentCaseInsensitiveStringsMap}
import com.typesafe.scalalogging.slf4j.Logging
import java.io.{ByteArrayInputStream, FileInputStream, InputStream, File}
import scala.io.Codec

case class S3RequestBuilder(credentials:Credentials, bucket:Option[Bucket], path:String) extends RequestBuilder with Logging {

  private val _request:Request = request

  private var content:Option[InputStream] = None
  private var contentType:Option[String] = None
  implicit private var contentCodec:Codec = Codec.UTF8

  setUrl(s"https://${Signer.host(bucket)}$path")

  def <<<(file:File)(implicit codec:Codec):S3RequestBuilder = {
    MimeTypes.forFileName(file.getName).foreach(setContentType(_))
    upload(file)(codec)
  }

  def setContentType(contentType:String):S3RequestBuilder = {
    this.contentType = Some(contentType)
    this.contentType.foreach(setHeader("Content-Type", _))
    this
  }

  def upload(file:File)(codec:Codec):S3RequestBuilder = {
    content = Some(new FileInputStream(file))
    contentCodec = codec
    setMethod("PUT").setBody(file)
    this
  }

  def PUT(body:String):S3RequestBuilder = {
    content = Some(new ByteArrayInputStream(body.getBytes(DEFAULT_ENCODING)))
    setMethod("PUT").setBody(body)
    this
  }

  override def build:Request = {
    setHeaders(Signer(credentials, bucket, _request.getMethod, path, contentType, content, _request.getHeaders))
    content.map(_.close)
    val finalRequest = super.build
    logger.trace("build()> {}", finalRequest)
    finalRequest
  }

  implicit def headersToJava(headers:S3.Headers):java.util.Map[String, java.util.Collection[String]] = {
    import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}
    mapAsJavaMap(headers.mapValues(seqAsJavaList))
  }

  implicit def javaToHeaders(map:FluentCaseInsensitiveStringsMap):S3.Headers = {
    import scala.collection.JavaConversions.{mapAsScalaMap, asScalaBuffer}
    mapAsScalaMap(map).mapValues(asScalaBuffer).toMap
  }
}

import com.typesafe.scalalogging.slf4j.Logging

object S3RequestBuilder extends Logging {

  def apply(bucket:Bucket, path:String) = {
    logger.trace("apply(bucket={}, path={})", bucket, path)
    new S3RequestBuilder(bucket.credentials, Some(bucket), path)
  }

  def apply(bucket:Bucket) = {
    logger.trace("apply(bucket={})", bucket)
    new S3RequestBuilder(bucket.credentials, Some(bucket), "/")
  }

  def apply(credentials:Credentials) = {
    logger.trace("apply(credentials={})", credentials)
    new S3RequestBuilder(credentials, None, "/")
  }
}
