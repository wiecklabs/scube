package scube

import scala.concurrent.Future
import java.io.{FileInputStream, File}
import dispatch._, Defaults._
import scala.util.{Try, Success, Failure}
import com.typesafe.scalalogging.slf4j.Logging

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
        Some(FileItem(path)(response.getResponseBodyAsStream))
      }
    }
  }

  def put(path:String):File => Future[FileItem] = put(path, None)

  def put(path:String, acl:ACL.ACL):File => Future[FileItem] = put(path, Some(acl))

  def put(path:String, acl:Option[ACL.ACL])(file:File):Future[FileItem] = {
    Http(S3RequestBuilder(this, path ensureStartsWith '/') <<< file OK as.String) map { result =>
      FileItem(path)(new FileInputStream(file))
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

  def copyFile(sourcePath:String, destinationPath:String):Future[Try[Unit]] =
    copyFile(this, sourcePath, destinationPath)

  def copyFile(sourceBucket:Bucket, sourcePath:String, destinationPath:String):Future[Try[Unit]] = {

    val source = (sourceBucket.name ensureStartsWith '/') + (sourcePath ensureStartsWith '/')
    val destination = destinationPath ensureStartsWith '/'

    Http(S3RequestBuilder(this, destination)
      .setHeader("x-amz-copy-source", source)
      .PUT OK(response => response)).either map {
      case Left(e) => {
        logger.error("Unhandled error status", e)
        throw e
      }
      case Right(response) if response.getStatusCode == 200 => {
        logger.info("Copied '{}' to '{}' in bucket {}", source, destination, name)
        Success()
      }
      case Right(response) => {
        logger.error("UnhandledResponse OK status: {}", response)
        Failure(new UnhandledResponse(response))
      }
    }
  }
}