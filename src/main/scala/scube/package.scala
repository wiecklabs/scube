import scala.util.Try

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

    def ensureEndsWith(c:Char):String = ensureEndsWith(c.toString)
    def ensureEndsWith(suffix:String):String = if(s.endsWith(suffix)) s else s + suffix
  }

  object Integer {
    def unapply(s:String):Option[Int] = Try(s.toInt).toOption
  }
}