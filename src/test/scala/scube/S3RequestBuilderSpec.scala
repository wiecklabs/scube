package scube

import dispatch._

class S3RequestBuilderSpec extends test.Spec {

  "A S3RequestBuilder should" - {

    val bucketName = "wieck-test-1"
    val bucket = Bucket(bucketName)(Credentials.default)

    "take Credentials, an optional Bucket and a path" in {
      new S3RequestBuilder(Credentials.default, Some(bucket), "/")
    }

    "apply" - {
      "with a Bucket and a path" in {
        S3RequestBuilder(bucket, "/")
      }

      "with credentials only" in {
        S3RequestBuilder(Credentials.default)
      }
    }

    "set the host to s3.amazonaws.com" in {
      S3RequestBuilder(Credentials.default).build().getUrl must equal("https://s3.amazonaws.com/")
    }
  }
}
