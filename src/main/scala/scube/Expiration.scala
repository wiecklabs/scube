package scube

import java.util.Date
import scala.xml._

sealed abstract class Expiration {
  def toXml:NodeSeq
}

object Expiration {
  def apply(days:Int):Expiration = DaysExpiration(days)
  def apply(date:Date):Expiration = DateExpiration(date)

  def unapply(xml:Node):Option[Expiration] = Utility.trim(xml) match {
    case <Expiration><Date>{Text(RFC822(date))}</Date></Expiration> => Some(DateExpiration(date))
    case <Expiration><Days>{Text(Integer(days))}</Days></Expiration> => Some(DaysExpiration(days))
    case _ => None
  }

  def foo(xml:Node) = {
    RFC822.unapply(xml \ "Date" text).get
  }
}

case class DaysExpiration(days:Int) extends Expiration {
  def toXml =
    <Expiration>
      <Days>{days}</Days>
    </Expiration>
}

case class DateExpiration(date:Date) extends Expiration {
  def toXml =
    <Expiration>
      <Date>{RFC822(date)}</Date>
    </Expiration>
}

object NoExpiration extends Expiration {
  def toXml = NodeSeq.Empty
}