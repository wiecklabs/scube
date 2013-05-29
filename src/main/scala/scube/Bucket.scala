package scube

import scala.concurrent.Future
import java.io.File
import dispatch._, Defaults._
import scala.util.{Try, Success, Failure}
import com.typesafe.scalalogging.slf4j.Logging
import scala.io.Source

case class Bucket(name: String,
                  acl:Option[ACL.ACL] = None,
                  delimiter:String = "/")
                 (implicit val credentials:Credentials) extends Logging {

  import S3.UnhandledResponse

  override def toString = name

  def apply(path:String):Future[Option[FileItem]] = {
    Http(S3RequestBuilder(this, path ensureStartsWith '/') OK(response => response)).either map {
      case Left(StatusCode(404)) => None
      case Left(e:Throwable) => throw e
      case Right(response) => {
        Some(FileItem(path)(Source.fromInputStream(response.getResponseBodyAsStream)))
      }
    }
  }

  def put(path:String):File => Future[FileItem] = put(path, None)

  def put(path:String, acl:ACL.ACL):File => Future[FileItem] = put(path, Some(acl))

  def put(path:String, acl:Option[ACL.ACL])(file:File):Future[FileItem] = {
    Http(S3RequestBuilder(this, path ensureStartsWith '/').PUT <<< file OK as.String) map { result =>
      FileItem(path)(Source.fromFile(file))
    }
  }

  def delete(path:String):Future[Try[Unit]] = {
    logger.debug("delete(path={}", path)
    Http(S3RequestBuilder(this, path ensureStartsWith '/').DELETE OK(response => response)).either map {
      case Left(e @ StatusCode(404)) => {
        logger.warn("Missing path: {}", path)
        Failure(e)
      }
      case Left(e) => {
        logger.error("Unhandled error status", e)
        throw e
      }
      case Right(response) if response.getStatusCode == 204 => {
        logger.info("Deleted item: {}", path)
        Success()
      }
      case Right(response) => {
        logger.error("UnhandledResponse OK status: {}", response)
        Failure(new UnhandledResponse(response))
      }
    }
  }

  def list:Future[Seq[String]] = {
    Http(S3RequestBuilder(this) OK as.xml.Elem).either map {
      case Left(StatusCode(404)) => List.empty[String]
      case Left(e:Throwable) => throw e
      case Right(bucket) => {
        logger.trace("Bucket contents: {}", bucket)
        bucket \\ "Contents" \ "Key" map(_.text)
      }
    }
  }

  def clear:Future[Seq[Try[Unit]]] = {
    list flatMap { paths =>
      Future.sequence(paths.map { delete(_) })
    }
  }

  private implicit class WrappedString(s:String) {
    def ensureStartsWith(c:Char):String = ensureStartsWith(c.toString)
    def ensureStartsWith(prefix:String):String = if(s.startsWith(prefix)) s else prefix + s
  }
}