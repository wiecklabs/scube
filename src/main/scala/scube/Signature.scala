package scube

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Signature {
  def apply(key: String, canonicalRequest:String):String = {
    val mac = Mac getInstance "HmacSHA1"
    mac init new SecretKeySpec(key getBytes DEFAULT_ENCODING, mac.getAlgorithm)
    Base64(mac doFinal canonicalRequest.getBytes(DEFAULT_ENCODING))
  }
}
