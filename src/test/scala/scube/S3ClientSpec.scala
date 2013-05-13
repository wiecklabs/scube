package scube

import com.ning.http.client.RequestBuilder

class S3ClientSpec extends test.Spec {

  import dispatch._
  import scala.collection.JavaConversions._

  "A S3Client should" - {
    "add an Authorization header" in {
      S3Client(_.GET).build().getHeaders.keySet must contain("Authorization")
    }

    "set Host header to s3.amazonaws.com" in {
      S3Client(_.GET).build().getHeaders.get("Host") must contain("s3.amazonaws.com")
    }

    "specify a bucket" in {
      S3Client(_.setUrl("wieck-test-1")).GET
    }

    "things" in {
      case class Bucket(name:String) extends RequestBuilder

      Bucket("wieck-test-1").GET
    }
  }

}
