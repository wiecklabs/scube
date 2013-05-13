package scube

class APISpec extends test.Spec {

  "The API for" - {

    "S3 should" - {
      trait Credentials
      trait Bucket

      trait Account {
        def buckets:Seq[Bucket]
      }

      case class S3(implicit c:Credentials) extends Account {
        def buckets = ???
      }

      implicit object AwsCredentials extends Credentials

      "list buckets" in {
        S3().buckets
      }
    }

    "buckets should" - {

      "have meta-data" - {

      }

      "list" in {
        "/europe/cars/mustang/photos"

        ":channel*/photos"

        "/europe/cars/mustang/this-is-my-slug"

        ":channel*/photos/:slug"

        "/admin/photos/:slug"

        ":channel*/photos/:slug"

        ":path*"
      }

      "create" - {

        "with default permissions" in { }

        "with custom permissions" in { }

      }

      "get meta-data" in { }

      "delete" in { }
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
