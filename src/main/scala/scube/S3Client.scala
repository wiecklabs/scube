package scube

import dispatch._
import com.ning.http.client.RequestBuilder

class S3Client(request:Req => Req) {
  def hostName = "s3.amazonaws.com"
  def host = :/(hostName).secure / "1.1"
  def sign:Req = {
    request(host) match { case request =>
      request.setHeader("Host", hostName)
    }
  }
}

object S3Client {
  def apply(request:Req => Req):Req = new S3Client(request).sign
}