package test

import org.scalatest._
import dispatch._, Defaults._
import scube.S3
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

trait Spec extends FreeSpec with MustMatchers with BeforeAndAfterAll {

  def await[A](f:Future[A]):A = Await.result(f, Duration.Inf)

  def deleteBucket(name:String) = S3(name).flatMap {
    case None => Future.successful()
    case Some(bucket) => bucket.clear map { _ => S3.delete(bucket) }
  }
}