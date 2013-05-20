package scube

import java.util.Date
import dispatch._, Defaults._


class APISpec extends test.Spec {

  "The API for" - {

    import scala.concurrent.{Future, Await}
    import scala.concurrent.duration.Duration

    import scala.util.{Try, Success, Failure}

    def await[A](f:Future[A]):A = Await.result(f, Duration.Inf)

    val bucketName = "wieck-test-1"

    "S3 should" - {

      "create a bucket with default permissions" in await {
        S3.put(bucketName) flatMap { _ =>
          S3(bucketName).map {
            case Some(bucket) => bucket.acl must equal(ACL.PUBLIC_READ)
            case None => fail
          }
        }
      }

      "delete a bucket" in await {
        S3(bucketName).map {
          case Some(bucket) => S3.delete(bucket)
          case None => fail
        }
      }

      "create a bucket with custom permissions" in await {
        S3.put(bucketName, ACL.AUTHENTICATED_READ) flatMap { _ =>
          S3(bucketName) map {
            case Some(Bucket(_, Some(acl), _, _)) => acl must equal(ACL.AUTHENTICATED_READ)
            case _ => fail
          }
        }
      }

      "list buckets" in await {
        S3.buckets zip S3(bucketName) map {
          case (buckets, Some(bucket)) => buckets must contain(bucket)
          case _ => fail
        }
      }

    }

    "buckets should" - {

      "have meta-data" in {
        S3(bucketName) map {
          case Some(Bucket(name, acl, createdAt, delimiter)) => {
            name must equal(bucketName)
            acl must equal(ACL.PUBLIC_READ)
          }
          case None => fail
        }
      }


    }

    "files should" - {

      "have meta-data" - {

      }

      "list" in { }

      "create" - {

        "with inherited bucket permissions" in { }

        "with custom permissions" in { }

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
