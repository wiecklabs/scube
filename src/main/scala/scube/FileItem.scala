package scube

import scala.io.BufferedSource

case class FileItem(path:String)(source:BufferedSource) {
  override def toString = path

  def getBytes = source map(_.toByte) toArray

  def close = source.close
}