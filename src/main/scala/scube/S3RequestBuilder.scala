package scube

import dispatch._
import com.ning.http.client.{RequestBuilder, Request, RequestBuilderBase, FluentCaseInsensitiveStringsMap}
import com.typesafe.scalalogging.slf4j.Logging

class S3RequestBuilder(credentials:Credentials, bucket:Option[Bucket], path:String) extends RequestBuilder with Logging {

  private val _request:Request = request

  setUrl(s"https://${Signer.host(bucket)}$path")

  override def build = {
    setHeaders(Signer(credentials, bucket, _request.getMethod, path, _request.getHeaders))
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
