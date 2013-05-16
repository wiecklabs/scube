package scube

import java.util.Date
import dispatch._, Defaults._


//class APISpec extends test.Spec {
//
//  "The API for" - {
//
//    import scala.concurrent.{Future, Await}
//    import scala.concurrent.duration.Duration
//
//    import scala.util.{Try, Success, Failure}
//
//    def await[A](f:Future[A]):A = Await.result(f, Duration.Inf)
//
//    trait Credentials
//
//    case class Bucket(name:String, acl:ACL.ACL = ACL.PUBLIC_READ, createdAt:Date = new Date)(c:Credentials)
//
//    trait Account {
//      def buckets(implicit c:Credentials):Future[Seq[Bucket]]
//
//      def apply(bucketName:String)(implicit c:Credentials):Future[Option[Bucket]] = ???
//
//      def put(bucketName:String)(implicit c:Credentials):Future[Try[Bucket]] = put(bucketName, ACL.PUBLIC_READ)(c)
//
//      def put(bucketName:String, acl:ACL.ACL)(implicit c:Credentials):Future[Try[Bucket]]
//
//      def delete(bucket:Bucket)(implicit c:Credentials):Future[Try[Unit]]
//    }
//
//    object S3 extends Account {
//      def buckets(implicit c:Credentials):Future[Seq[Bucket]] =
//        Future.successful(Seq.empty[Bucket])
//
//      def put(bucketName:String, acl:ACL.ACL)(implicit c:Credentials):Future[Try[Bucket]] =
//        Future.successful(Success(new Bucket(bucketName)(c)))
//
//      def delete(bucket:Bucket)(implicit c:Credentials):Future[Try[Unit]] =
//        Future.successful(Success())
//    }
//
//    implicit object AwsCredentials extends Credentials
//
//    val bucketName = "wieck-test-1"
//
//    "S3 should" - {
//
//      "create a bucket with default permissions" in await {
//        S3.put(bucketName) flatMap { _ =>
//          S3(bucketName) map { case Some(bucket) =>
//            bucket.acl must equal(ACL.PUBLIC_READ)
//          }
//        }
//      }
//
//      "delete a bucket" in await {
//        S3(bucketName).map {
//          case Some(bucket) => S3.delete(bucket)
//        }
//      }
//
//      "create a bucket with custom permissions" in await {
//        S3.put(bucketName, ACL.AUTHENTICATED_READ) flatMap { _ =>
//          S3(bucketName) map { case Some(bucket) =>
//            bucket.acl must equal(ACL.AUTHENTICATED_READ)
//          }
//        }
//      }
//
//      "list buckets" in await {
//        S3.buckets zip S3(bucketName) map { case (buckets, Some(bucket)) =>
//          buckets must contain(bucket)
//        }
//      }
//
//    }
//
//    "buckets should" - {
//
//      "have meta-data" in {
//        S3(bucketName) map {
//          case Some(Bucket(name, acl, createdAt)) => {
//            name must equal(bucketName)
//            acl must equal(ACL.PUBLIC_READ)
//          }
//        }
//      }
//
//
//    }
//
//    "files should" - {
//
//      "have meta-data" - {
//
//      }
//
//      "list" in { }
//
//      "create" - {
//
//        "with inherited bucket permissions" in { }
//
//        "with custom permissions" in { }
//
//      }
//
//      "get" - {
//
//        "meta-data" in { }
//
//        "a file stream" in { }
//
//        "an authenticated url" in { }
//      }
//
//      "delete" in { }
//
//      "bulk delete" in { }
//
//      "copy" in { }
//    }
//
//  }
//}
