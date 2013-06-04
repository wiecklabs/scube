package scube

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import dispatch._, Defaults._
import com.typesafe.scalalogging.slf4j.Logging
import com.ning.http.client.Response
import scala.xml.Elem
import scala.xml.NodeSeq

object S3 extends Account with Logging {

  type Headers = Map[String, Seq[String]]

  class UnhandledResponse(response:Response) extends RuntimeException(response.getStatusText)

  class DeserializationException(xml:NodeSeq) extends RuntimeException("Could not parse " + xml)

  def apply(bucketName: String)(implicit c: Credentials): Future[Option[Bucket]] = {
    val bucket = Bucket(bucketName)

    Http(S3RequestBuilder(bucket).HEAD OK(response => response)).either map {
      case Left(StatusCode(404)) => None
      case Left(e:Throwable) => throw e
      case Right(grants) => Some(bucket)
    }
  }

  def buckets(implicit c:Credentials):Future[Seq[Bucket]] =
    for(response <- Http(S3RequestBuilder(c) OK as.xml.Elem))
    yield response \\ "Bucket" \ "Name" map { x => Bucket(x.text)(c) }

  def put(bucketName:String, acl:ACL.ACL)(implicit c:Credentials):Future[Try[Bucket]] = {
    logger.info("put(bucketName={}, acl={})", bucketName, acl)

    val bucket = Bucket(bucketName, Some(acl))
    val request = S3RequestBuilder(bucket).setHeader("x-amz-acl", acl.toString).PUT

    Http(request OK as.String).either.map {
      case Left(e) => Failure(e)
      case Right(result) => Success(bucket)
    }
  }

  def delete(bucket:Bucket)(implicit c:Credentials):Future[Try[Unit]] = {
    logger.error("delete(bucket={})", bucket)
    Http(S3RequestBuilder(bucket).DELETE OK(response => response)).either map {
      case Left(e @ StatusCode(404)) => {
        logger.debug("Missing bucket: {}", bucket)
        Failure(e)
      }
      case Left(e) => {
        logger.error("Unhandled error status", e)
        throw e
      }
      case Right(response) if response.getStatusCode == 204 => {
        logger.info("Deleted bucket: {}", bucket)
        Success()
      }
      case Right(response) => {
        logger.error("UnhandledResponse OK status: {}", response)
        Failure(new UnhandledResponse(response))
      }
    }
  }
}