package scube

import java.security.MessageDigest
import java.io.File
import scala.io.Source

object ContentMD5 {
  def apply(bytes:Array[Byte]):String = {
    val md = MessageDigest getInstance "MD5"
    md update bytes
    Base64(md.digest)
  }

  def apply(file:File):String = {
    val md = MessageDigest getInstance "MD5"
    val source = Source.fromFile(file)
    source.withClose(() => source.foreach(md update _.toByte))
    Base64(md.digest)
  }
}
