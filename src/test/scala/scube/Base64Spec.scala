package scube

class Base64Spec extends test.Spec {

  "Base64 should" - {
    "encode bytes" in {
      val data = "Twinkle, twinkle little star...".getBytes(DEFAULT_ENCODING)
      val expectation = "VHdpbmtsZSwgdHdpbmtsZSBsaXR0bGUgc3Rhci4uLg=="

      Base64(data) must equal(expectation)
    }
  }
}
