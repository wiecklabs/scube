package scube

import java.security.MessageDigest

object ContentMD5 {
  def apply(bytes:Array[Byte]):String = {
    val md = MessageDigest getInstance "MD5"
    md update bytes
    Base64(md.digest)
  }
}
