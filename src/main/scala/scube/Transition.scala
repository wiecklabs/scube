package scube

import java.util.Date
import scala.xml._

sealed abstract class Transition {
  def toXml:NodeSeq
}

object Transition {
  def apply(days:Int):Transition = DaysTransition(days)
  def apply(date:Date):Transition = DateTransition(date)

  def unapply(xml:Node):Option[Transition] = Utility.trim(xml) match {
    case <Transition><Date>{Text(RFC822(date))}</Date>{_*}</Transition> => Some(DateTransition(date))
    case <Transition><Days>{Text(Integer(days))}</Days>{_*}</Transition> => Some(DaysTransition(days))
    case _ => None
  }
}

case class DaysTransition(days:Int) extends Transition {
  def toXml =
    <Transition>
      <Days>{days}</Days>
      <StorageClass>GLACIER</StorageClass>
    </Transition>
}

case class DateTransition(date:Date) extends Transition {
  def toXml =
    <Transition>
      <Date>{RFC822(date)}</Date>
      <StorageClass>GLACIER</StorageClass>
    </Transition>
}

object NoTransition extends Transition {
  def toXml = NodeSeq.Empty
}