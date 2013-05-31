package scube

import scala.io.Source
import java.io.InputStream

case class FileItem(path:String)(input:InputStream) {
  override def toString = path

  def source = Source.fromInputStream(input)

  def getBytes = using(source)(_ map(_.toByte) toArray)

  def size = using(source)(_.size)
}