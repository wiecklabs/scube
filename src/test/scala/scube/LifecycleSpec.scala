package scube

import dispatch._, Defaults._
import scala.util.Failure

class LifecycleSpec extends test.Spec {

  val bucketName = "wieck-test-lifecycle"
  var bucket:Bucket = _

  override def beforeAll = {
    bucket = await(S3.put(bucketName)).getOrElse(fail)
  }

  override def afterAll = await {
    deleteBucket(bucketName)
  }

  "A bucket should" - {

    "get it's lifecycle" in await {
      bucket.lifecycle map { lifecycle =>
        lifecycle.rules must be('empty)
      }
    }

    "have a lifecycle that" - {

      "is clearable" in await {
        bucket.lifecycle.flatMap(_.clear) map {
          case Failure(e) => fail(e)
          case _ => ()
        }
      }

      "can create a rule" in await {
        val rule = Rule("tmp-expiration", "tmp/").expires(1)
        bucket.put(rule) flatMap { _ =>
          bucket.lifecycle map { lifecycle =>
            lifecycle.rules must have size(1)
          }
        }
      }

      "can create multiple rules" in await {
        val rules = Seq(
          Rule("archive", "glacier/").transitions(30),
          Rule("tmp-expiration", "tmp/").expires(1))

        bucket.put(rules:_*) flatMap { _ =>
          bucket.lifecycle map { lifecycle =>
            lifecycle.rules must equal(rules)
          }
        }
      }

      "can extend rules" in await {
        val rule = Rule("cleanup", "trash/").expires(1)

        bucket.lifecycle flatMap { lifecycle =>
          bucket.put(lifecycle ++ rule) map { combined =>
            combined.rules must have size(3)
          }
        }
      }
    }
  }
}
