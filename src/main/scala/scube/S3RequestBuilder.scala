package scube

import dispatch._
import com.ning.http.client.RequestBuilder
import com.typesafe.scalalogging.slf4j.Logging

class S3RequestBuilder extends RequestBuilder {

  def sign = {
    request.getMethod
  }

  override def build = {
//    addHeader("Authorization", s"AWS $key:${Signer(secret, method, host, date, path)}")
    super.build
  }
}

object S3RequestBuilder extends Logging {

  type Headers = Map[String, Seq[String]]

  def apply(bucket:Option[String], path:String) = {
    val uri = new S3Uri(bucket, path).toString
    logger.trace("apply(bucket={}, path={}) : uri={}", bucket, path, uri)
    new S3RequestBuilder().setUrl(uri)
  }

  class S3Uri(bucket:Option[String], path:String) {
    val host = "s3.amazonaws.com"

    override def toString = {
      bucket.fold(s"https://$host$path")(bucket => s"https://$bucket.$host$path")
    }
  }
}
