package scube

import java.util.Date
import scala.xml._
import scube.S3.DeserializationException

case class Rule(id:String,
                 prefix:Option[String],
                 transition:Transition,
                 expiration:Expiration,
                 enabled:Boolean = true) {

  def expires(days:Int):Rule = copy(expiration = Expiration(days))
  def expires(date:Date):Rule = copy(expiration = Expiration(date))

  def transitions(days:Int):Rule = copy(transition = Transition(days))
  def transitions(date:Date):Rule = copy(transition = Transition(date))

  def toXml = Utility.trim(<Rule>
      <ID>{id}</ID>
      <Prefix>{prefix.getOrElse("")}</Prefix>
      <Status>Enabled</Status>
      {transition.toXml}
      {expiration.toXml}
    </Rule>)
}

object Rule {
  def apply(id:String, prefix:String):Rule = {
    this(id, Some(prefix))
  }

  def apply(id:String, prefix:Option[String]):Rule = {
    this(id, prefix, NoTransition, NoExpiration)
  }

  def unapply(xml:Node):Option[Rule] = Utility.trim(xml) match {
    case <Rule><ID>{Text(id)}</ID><Prefix>{Text(prefix)}</Prefix><Status>{Text(status)}</Status>{Transition(transition)}</Rule> =>
      Some(Rule(id, Option(prefix), transition, NoExpiration, status == "Enabled"))
    case <Rule><ID>{Text(id)}</ID><Prefix>{Text(prefix)}</Prefix><Status>{Text(status)}</Status>{Expiration(expiration)}</Rule> =>
      Some(Rule(id, Option(prefix), NoTransition, expiration, status == "Enabled"))
    case <Rule><ID>{Text(id)}</ID><Prefix>{Text(prefix)}</Prefix><Status>{Text(status)}</Status>{Transition(transition)}{Expiration(expiration)}</Rule> =>
      Some(Rule(id, Option(prefix), transition, expiration, status == "Enabled"))
    case <Rule><ID>{Text(id)}</ID><Prefix>{Text(prefix)}</Prefix><Status>{Text(status)}</Status>{Expiration(expiration)}{Transition(transition)}</Rule> =>
      Some(Rule(id, Option(prefix), transition, expiration, status == "Enabled"))
    case _ => None
  }
}

object Rules {
  def unapply(nodes:Seq[Node]):Option[Seq[Rule]] = {

    val rules:Seq[Rule] = nodes collect { case Rule(rule) => rule }

    if (rules.isEmpty || rules.length != nodes.length) {
      None
    } else {
      Some(rules)
    }
  }
}