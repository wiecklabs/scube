package scube

import scala.concurrent.Future
import scala.util.{Success, Try}
import dispatch._, Defaults._

object S3 extends Account {

  type Headers = Map[String, Seq[String]]

  def apply(bucketName: String)(implicit c: Credentials): Future[Option[Bucket]] = {
    Http(S3RequestBuilder(Bucket(bucketName)) OK as.String).either.map {
      case Left(e) => throw e
      case Right(thing) => {
        println(thing)
        None
      }
    }
  }

  def buckets(implicit c:Credentials):Future[Seq[Bucket]] =
    for(response <- Http(S3RequestBuilder(c) OK as.xml.Elem))
    yield response \\ "Bucket" \ "Name" map { x => Bucket(x.text)(c) }

  def put(bucketName:String, acl:ACL.ACL)(implicit c:Credentials):Future[Try[Bucket]] =
    Future.successful(Success(new Bucket(bucketName)))

  def delete(bucket:Bucket)(implicit c:Credentials):Future[Try[Unit]] =
    Future.successful(Success())
}