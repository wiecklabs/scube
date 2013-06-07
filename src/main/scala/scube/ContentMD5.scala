package scube

import java.security.MessageDigest
import java.io.{InputStream, File}
import scala.io.{Codec, Source}

object ContentMD5 {
  def apply(bytes:Array[Byte]):String = digest(_ update bytes)

  def apply(file:File)(implicit codec:Codec):String = digest { md =>
    using(Source.fromFile(file)(codec))(_.foreach(md update _.toByte))
  }

  def apply(input:InputStream)(implicit codec:Codec):String = digest { md =>
    using(Source.fromInputStream(input)(codec))(_.foreach(md update _.toByte))
  }

  private def digest(f:MessageDigest => Unit):String = {
    val md = MessageDigest getInstance "MD5"
    f(md)
    Base64(md.digest)
  }
}
