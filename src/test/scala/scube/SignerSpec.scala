package scube

class SignerSpec extends test.Spec {

  "A Signer" - {

    "using canned request values should" - {
      val credentials = Credentials("bob", "s3kret")
      val bucket = Bucket("wieck-test-1")
      val method:String = "GET"
      val path:String = "/"
      val headers:S3RequestBuilder.Headers = Map.empty
      val contentType:Option[String] = None

      "sign the request" in {
        Signer(credentials, bucket, method, path, headers, contentType).keys must contain("Authorization")
      }
    }
  }
}
