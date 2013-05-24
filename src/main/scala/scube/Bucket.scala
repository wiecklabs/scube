package scube

import scala.concurrent.Future
import java.io.File
import dispatch._, Defaults._

case class Bucket(name: String,
                  acl:Option[ACL.ACL] = None,
                  delimiter:String = "/")
                 (implicit val credentials:Credentials) {
  override def toString = name

  def apply(path:String):Future[Option[S3File]] = {
    Http(S3RequestBuilder(this, path ensureStartsWith '/') OK(response => response)).either map {
      case Left(StatusCode(404)) => None
      case Left(e:Throwable) => throw e
      case Right(response) => {
        println(s"!!! -> $response")
        Some(S3File(path))
      }
    }
  }

  def put(path:String):File => Future[S3File] = put(path, None)

  def put(path:String, acl:ACL.ACL):File => Future[S3File] = put(path, Some(acl))

  def put(path:String, acl:Option[ACL.ACL])(file:File):Future[S3File] = {
    Http(S3RequestBuilder(this, path ensureStartsWith '/').PUT OK as.String) map { result =>
      println(result)
      S3File(path)
    }
  }

  private implicit class WrappedString(s:String) {
    def ensureStartsWith(c:Char):String = ensureStartsWith(c.toString)
    def ensureStartsWith(prefix:String):String = if(s.startsWith(prefix)) s else prefix + s
  }
}