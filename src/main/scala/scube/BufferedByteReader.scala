package scube

import java.io.{IOException, BufferedInputStream, InputStream}

// Lifted from: http://stackoverflow.com/questions/7773727/reading-a-large-file-in-functional-scala/7773984#7773984
class BufferedByteReader(input:BufferedInputStream) {
  val buffer = new Array[Byte](100000)
  var n = 0
  var i = 0

  def hasNext:Boolean = (i < n) || (n >= 0 && {
    n = input.read(buffer)
    i = 0
    hasNext
  })

  def next:Byte = {
    if(i < n) {
      val b = buffer(i)
      i += 1
      b
    } else if(hasNext) next
    else throw new IOException("Input stream empty")
  }
}

object BufferedByteReader extends Function[InputStream, BufferedByteReader] {
  def apply(input:InputStream):BufferedByteReader = new BufferedByteReader(new BufferedInputStream(input))

  implicit def readerAsIterator(bbr:BufferedByteReader) = new Iterator[Byte] {
    def hasNext = bbr.hasNext
    def next = bbr.next
  }
}