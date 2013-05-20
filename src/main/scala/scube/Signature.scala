package scube

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.typesafe.scalalogging.slf4j.Logging

object Signature extends Logging {
  def apply(key: String, canonicalRequest:String):String = {
    logger.trace("apply(key={}, canonicalRequest={})", key, canonicalRequest)
    val mac = Mac getInstance "HmacSHA1"
    mac init new SecretKeySpec(key getBytes DEFAULT_ENCODING, mac.getAlgorithm)
    Base64(mac doFinal canonicalRequest.getBytes(DEFAULT_ENCODING))
  }
}
