package scube

import java.security.MessageDigest
import java.io.{FileInputStream, InputStream, File}
import scala.io.Source

object ContentMD5 {
  def apply(bytes:Array[Byte]):String = digest(_ update bytes)

  def apply(file:File):String = digest { md =>
    using(new FileInputStream(file)) { input =>
      BufferedByteReader(input).foreach(md update _)
    }
  }

  def apply(input:InputStream):String = digest { md =>
    using(input)(BufferedByteReader(_).foreach(md update _))
  }

  private def digest(f:MessageDigest => Unit):String = {
    val md = MessageDigest getInstance "MD5"
    f(md)
    Base64(md.digest)
  }
}
