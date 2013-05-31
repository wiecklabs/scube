package object scube {
  val DEFAULT_ENCODING = "UTF-8"

  def using[T,S<:{def close()}](source:S)(f:S => T) = {
    val result = f(source)
    source.close
    result
  }

  implicit class WrappedString(s:String) {
    def ensureStartsWith(c:Char):String = ensureStartsWith(c.toString)
    def ensureStartsWith(prefix:String):String = if(s.startsWith(prefix)) s else prefix + s
  }
}