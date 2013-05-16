package scube

object Base64 {
  import sun.misc.BASE64Encoder
  val encoder = new BASE64Encoder()

  def apply(data:Array[Byte]):String = encoder.encode(data)
}
