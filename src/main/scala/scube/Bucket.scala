package scube

import scala.io.BufferedSource
import scala.concurrent.Future
import java.io.File

case class Bucket(name: String,
                  acl:Option[ACL.ACL] = None,
                  delimiter:String = "/")
                 (implicit val credentials:Credentials) {
  override def toString = name

  def <<<(file:File):Future[S3File] = put(file, None)

  def <<<(item:Pair[File, ACL.ACL]):Future[S3File] = put(item._1, Some(item._2))

  def put(file:File, acl:Option[ACL.ACL]):Future[S3File] =
    Future.successful(new S3File)
}