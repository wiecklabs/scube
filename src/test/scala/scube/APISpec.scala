package scube

import dispatch._, Defaults._
import java.io.File

class APISpec extends test.Spec {

  import scala.concurrent.Future
  import scala.util.{Try, Success, Failure}

  val bucketName = "wieck-test-1"

  val samplePath = "sample.txt"
  val sample = new File(S3.getClass.getResource("/" + samplePath).toURI)

  override def afterAll = await {
    deleteBucket(bucketName) zip
      deleteBucket(bucketName + "-files") zip
      deleteBucket("wieck-test-copy") zip
      deleteBucket("wieck-test-copy-1") zip
      deleteBucket("wieck-test-copy-2")
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

      "clear a bucket" in await {
        S3(bucketName) flatMap {
          case None => fail
          case Some(bucket) => bucket.clear flatMap {
            case _ => bucket.list.map(_ must be('empty))
          }
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

    "buckets should" - {
      "copy files" - {

        val source = "1-" + samplePath
        val destination = "2-" + samplePath

        def testCopyFile(sourceBucket:Bucket, destinationBucket:Bucket)(op: => Future[Try[Unit]]) = {
          sourceBucket.put(source)(sample) flatMap { file =>
            op map {
              case Failure(e) => fail(e)
              case _ => await {
                destinationBucket(destination) map {
                  case None => fail("Could not find copied file in destination!")
                  case Some(copyOfFile) => copyOfFile.size must equal(file.size)
                }
              }
            }
          }
        }

        "from another bucket" in await {
          val bucket1 = await(S3.put("wieck-test-copy-1")).getOrElse(fail)
          val bucket2 = await(S3.put("wieck-test-copy-2")).getOrElse(fail)

          testCopyFile(bucket1, bucket2)(bucket2.copyFile(bucket1, source, destination))
        }

        "within the same bucket" in await {
          val bucket = await(S3.put("wieck-test-copy")).getOrElse(fail)

          testCopyFile(bucket, bucket)(bucket.copyFile(source, destination))
        }
      }
    }

    "files should" - {

      val bucket = await(S3.put(bucketName + "-files")).getOrElse(fail)

      "return None for a missing File" in await {
        bucket("missing-" + samplePath).map(_ must be(None))
      }

      "create with inherited bucket permissions" in await {
        bucket.put(samplePath)(sample) map { file =>
          file.path must equal(samplePath)
        }
      }

      "list" in await {
        bucket.list.map(_.map(_.toString) must contain(samplePath))
      }

      "get" - {

        "meta-data" in { }

        "a file stream" in {
          bucket(samplePath) map {
            case None => fail
            case Some(file) => ContentMD5(file.getBytes) must equal(ContentMD5(sample))
          }
        }

        "an authenticated url" in { }
      }

      "delete" in await {
        bucket.delete(samplePath) map {
          case Failure(e) => fail(e)
          case _ => ()
        }
      }

      "create with custom permissions" in await {
        bucket.put(samplePath, ACL.AUTHENTICATED_READ)(sample) map { file =>
          file.path must equal(samplePath)
        }
      }
    }

  }
}
