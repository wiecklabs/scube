package scube

object RFC822 {
  import java.util.Date
  import java.util.{Locale, SimpleTimeZone}
  import java.text.SimpleDateFormat

  private val rfc822DateFormat = {
    val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    format setTimeZone new SimpleTimeZone(0, "UTC")
    format
  }

  def apply(date:Date) = rfc822DateFormat format date
}
