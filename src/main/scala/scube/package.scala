package object scube {
  val DEFAULT_ENCODING = "UTF-8"

  def using[T,S<:{def close()}](source:S)(f:S => T) = {
    val result = f(source)
    source.close
    result
  }
}