package scube

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Account {
  def buckets(implicit c:Credentials):Future[Seq[Bucket]]

  def apply(bucketName:String)(implicit c:Credentials):Future[Option[Bucket]] =
    Future.successful(None)

  def put(bucketName:String)(implicit c:Credentials):Future[Try[Bucket]] = put(bucketName, ACL.PUBLIC_READ)(c)

  def put(bucketName:String, acl:ACL.ACL)(implicit c:Credentials):Future[Try[Bucket]]

  def delete(bucket:Bucket)(implicit c:Credentials):Future[Try[Unit]]
}