package scube

import java.io.ByteArrayInputStream

class BufferedByteReaderSpec extends test.Spec {

  "A BufferedByteReader" - {

    "should produce the correct bytes" in {

      val expectation = Array[Byte](1,2,3,4)
      var i = 0

      import BufferedByteReader._

      BufferedByteReader(new ByteArrayInputStream(expectation)).foreach { byte =>
        i += 1
        byte must equal(i)
      }

      i must equal(4)
    }
  }
}
