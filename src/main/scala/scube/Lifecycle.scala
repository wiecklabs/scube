package scube

import dispatch._, Defaults._
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}
import com.typesafe.scalalogging.slf4j.Logging
import scala.xml._

case class Lifecycle(bucket:Bucket, rules:Seq[Rule]) extends Logging {
  import S3.UnhandledResponse

  def ++(rule:Rule):Lifecycle = copy(rules = this.rules :+ rule)
  def ++(rules:Seq[Rule]):Lifecycle = copy(rules = this.rules ++ rules)

  def toXml = <LifecycleConfiguration>{rules map(_.toXml)}</LifecycleConfiguration>

  def clear:Future[Try[Unit]] = {
    logger.debug("clear(bucket={})", bucket)
    Http(S3RequestBuilder(bucket, "/?lifecycle").DELETE OK(response => response)).either map {
      case Left(e @ StatusCode(404)) => Failure(e)
      case Left(e) => {
        logger.error("Unhandled error status", e)
        throw e
      }
      case Right(response) if response.getStatusCode == 204 => {
        logger.info("Deleted Lifecycle for {}", bucket)
        Success()
      }
      case Right(response) => {
        logger.error("UnhandledResponse OK status: {}", response)
        Failure(new UnhandledResponse(response))
      }
    }
  }

  def unapply(xml:Node):Option[Lifecycle] = Utility.trim(xml) match {
    case <LifecycleConfiguration>{rules @ _*}</LifecycleConfiguration> => rules match {
      case Rules(rules) => Some(Lifecycle(bucket, rules))
      case _ => None
    }
    case _ => None
  }
}

object Lifecycle {
  def empty(bucket:Bucket) = Lifecycle(bucket, Seq.empty[Rule])
}