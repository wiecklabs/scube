package scube

import dispatch._

class S3RequestBuilderSpec extends test.Spec {

  "A S3RequestBuilder should" - {

    val bucketName = "wieck-test-1"

    "take a bucket name and a path" in {
      S3RequestBuilder(Some(bucketName), "/")
    }

    "have an optional bucket name" in {
      S3RequestBuilder(None, "/")
    }

    "set the host to s3.amazonaws.com" in {
      S3RequestBuilder(None, "/").build().getUrl must equal("https://s3.amazonaws.com/")
    }

    "sign the request" - {

      val method = "GET"
      val path = "/"
      val host = "s3.amazonaws.com"
      val date = "Thu, 16 May 2013 14:39:59 UTC"

      val authorization = "AWS AKIAI6I6Q5KFBQ5OHQDA:oBrkOC/pHbbfNYZbkx0xd8Ri0aQ="

      "using a canned request" in {

      }
    }
  }
}
