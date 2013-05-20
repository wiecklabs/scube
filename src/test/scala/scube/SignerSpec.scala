package scube

class SignerSpec extends test.Spec {

  "A Signer" - {

    "using canned request values should" - {
      val credentials = Credentials("bob", "s3kret")
      val method:String = "GET"
      val path:String = "/"
      val headers:S3.Headers = Map.empty

      "sign the request" in {
        Signer(credentials, method, path, headers).keys must contain("Authorization")
      }
    }
  }
}
