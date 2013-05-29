package scube

import scala.io.Source
import java.io.{FileInputStream, File}

class ContentMD5Spec extends test.Spec {

  "ContentMD5 should" - {

    val path = "sample.txt"
    val sample = new File(S3.getClass.getResource("/" + path).toURI)

    "take a Byte Array" in {
      val data = "This is Sparta!".getBytes(DEFAULT_ENCODING)
      val expectation = "Kp4n5Lt3sSxX8zzYewHrsA=="

      ContentMD5(data) must equal(expectation)
    }

    "properly Base64 encode for a given input" in {
      val data = "All my exes live in Tejas".getBytes(DEFAULT_ENCODING)
      val expectation = "zbLFkTLkZEli0g0Dy29SNw=="

      ContentMD5(data) must equal(expectation)
    }

    "take a File object" in {
      val expectation = "jvGmmS0Wiu8Io9aXdfjFOQ=="
      ContentMD5(sample) must equal(expectation)
    }

    "take an InputStream" in {
      val expectation = "jvGmmS0Wiu8Io9aXdfjFOQ=="
      ContentMD5(new FileInputStream(sample)) must equal(expectation)
    }
  }
}
