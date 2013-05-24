package scube

import dispatch._, Defaults._
import java.io.File
import scala.io.{Codec, Source}

class APISpec extends test.Spec {

  import scala.concurrent.{Future, Await}
  import scala.concurrent.duration.Duration
  import scala.util.{Try, Success, Failure}

  def await[A](f:Future[A]):A = Await.result(f, Duration.Inf)

  val bucketName = "wieck-test-1"

  override def afterAll = await {
    def deleteBucket(name:String) = S3(name).flatMap {
      case None => Future.successful()
      case Some(bucket) => S3.delete(bucket)
    }

    deleteBucket(bucketName) zip
      deleteBucket(bucketName + "-files")
  }

  "The API for" - {

    "S3 should" - {

      "return None for a missing bucket" in await {
        S3(bucketName + "-missing").map(_ must be(None))
      }

      "fail to delete a missing bucket" in await {
        S3.delete(Bucket(bucketName + "-missing")) map {
          case Failure(StatusCode(404)) => ()
          case Failure(e) => fail(e)
          case Success(_) => fail
        }
      }

      "create a bucket with default permissions" in await {
        S3.put(bucketName) flatMap { _ =>
          Http(S3RequestBuilder(Bucket(bucketName)).copy(path = "/?acl") OK as.xml.Elem).either map {
            case Left(e) => throw e
            case Right(xml) => {
              xml \\ "Grant" filter { grant =>
                (grant \\ "URI").text.endsWith("AllUsers")
              } map(_ \\ "Permission" text) must contain("READ")
            }
          }
        }
      }

      "list buckets" in await {
        S3.buckets map { buckets =>
          buckets.map(_.name) must contain(bucketName)
        }
      }

      "delete a bucket" in await {
        S3.delete(Bucket(bucketName)) map {
          case Failure(e) => fail(e)
          case _ => ()
        }
      }

      "create a bucket with custom permissions" in await {
        S3.put(bucketName, ACL.AUTHENTICATED_READ) flatMap {
          case Failure(e) => throw e
          case Success(bucket) => {
            Http(S3RequestBuilder(Bucket(bucketName)).copy(path = "/?acl") OK as.xml.Elem).either map {
              case Left(e) => throw e
              case Right(xml) => {
                xml \\ "Grant" filter { grant =>
                  (grant \\ "URI").text.endsWith("AuthenticatedUsers")
                } map(_ \\ "Permission" text) must contain("READ")
              }
            } map(_ => S3.delete(bucket))
          }
        }
      }
    }

    "files should" - {

      val bucket = await(S3.put(bucketName + "-files")).getOrElse(fail)

      val path = "/sample.txt"
      val sample = new File(S3.getClass.getResource(path).toURI)

      "return None for a missing File" in await {
        bucket(path).map(_ must be(None))
      }

      "list" in { }

      "create" - {

        "with inherited bucket permissions" in await {
          bucket.put(path)(sample) map { file =>
            file.path must equal(path)
          }
        }

        "with custom permissions" in await {
          bucket.put(path, ACL.AUTHENTICATED_READ)(sample) map { file =>
            file.path must equal(path)
          }
        }

      }

      "get" - {

        "meta-data" in { }

        "a file stream" in { }

        "an authenticated url" in { }
      }

      "delete" in { }

      "bulk delete" in { }

      "copy" in { }
    }

  }
}
