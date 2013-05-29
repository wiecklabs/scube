package scube

import java.security.MessageDigest
import java.io.{InputStream, File}
import scala.io.Source

object ContentMD5 {
  def apply(bytes:Array[Byte]):String = digest(_ update bytes)

  def apply(file:File):String = digest { md =>
    using(Source.fromFile(file))(_.foreach(md update _.toByte))
  }

  def apply(input:InputStream):String = digest { md =>
    using(Source.fromInputStream(input))(_.foreach(md update _.toByte))
  }

  private def digest(f:MessageDigest => Unit):String = {
    val md = MessageDigest getInstance "MD5"
    f(md)
    Base64(md.digest)
  }
}
