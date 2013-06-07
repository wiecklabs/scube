# Scube

Scube is an S3 API for Scala.

## Features

* Create a Bucket
* Set a canned ACL for the Bucket
* Set Lifecycle Rules on the Bucket (for file Expiration or Transitions to Amazon Glacier)
* Delete Buckets
* Clear Bucket contents
* Upload Files
* Delete Files
* Set a canned ACL for a File

You can see examples of the features in the `APISpec`.

## API

The Scube API wrapped up in Futures. If you have experience with other Future based libraries, you should feel at home.

### Conventions

* `Future[Try[Unit]]`: You can think of this as a pass/fail, like a `Future[Boolean]`, but with a little extra detail on any failures that might occur.
* `Future[Lifecycle]`: Operations like these try to fail gracefully. So if your Bucket doesn't have a Lifecycle already defined for example, then you'll get an empty one back.
* `Future[Option[FileItem]]`: This will try to fail gracefully, but return a `None` if the item doesn't exist.

We handle the most common errors for you (`404`'s for example) and try to provide a basic convention.

#### Buckets

```scala
S3.put("cats") flatMap { // Since S3.delete(Bucket) returns a Future
  case Failure(e) => throw e
  case Success(bucket) => {
    println(s"This is my $bucket!")
    S3.delete(bucket) map {
      case Failure(e) => throw e
      case Success => ()
    }
  }
}
```

Or...

```scala
for {
  bucket <- S3.put("cats")
  result <- S3.delete(bucket.get)
} yield result.get
```

Other than the `println` they do the same thing.

Here's how to clear a bucket:

```scala
for {
  Some(bucket) <- S3("cats")
  Success(result) <- bucket.clear
} yield result
```

And here's how to create an expiration rule on a bucket:

```scala
for {
  Some(bucket) <- S3("cats")
  lifecycle <- bucket.lifecycle
} yield {
  // Lifecycles are replace-only.
  // You can't update them so this:
  bucket.put(lifecycle ++ Rule("tmp", "tmp/").expires(1))
  // Would preserve any existing Rules by concatenating
  // your new rule to the set of existing Rules and
  // writing them all. While this would overwrite any
  // existing rules with just your new rule:
  bucket.put(Rule("tmp", "tmp/").expires(1))
}
```

#### Files

```scala
val files:Future[Seq[String]] = for {
  Some(bucket)  <- S3("cats")
  file          <- bucket.put("feline.jpg")(new File("feline.jpg"))
  _             <- bucket.copyFile(file.path, "roger.jpg")
  _             <- bucket.delete(file.path)
  files         <- bucket.list
} yield files
```

Sample Use Case: An Expiration Rule such as the `"tmp"` Rule we defined previously, in conjunction with
`copyFile` could come in handy for AJAX uploads in a Web Application.

First, the User drops a file onto an AJAX upload region. Our Application PUTs that up to S3 under
a `tmp/` path: `bucket.put("tmp/" + file.getName)(file)`

Then when the User submits the Web Form for the Photo's Title, Caption, etc, we _copy_ it to a permanent
location: `bucket.copyFile("tmp/" + file.getName, file.getName)`

We don't have to worry about the original upload location since our Expiration Rule will cause it to be
deleted in about a day. If the User uploaded a file, but didn't follow through with the form
submission we can depend on the Rule to handle cleaning up the orphaned upload as well (again, after 1 day,
as specified in our Rule).

##### Large File Support

The File handling interfaces return `InputStream`s, so should work fine on large files,
or working with files larger than your available memory. For example, say you want to
MD5 a 10Gb video that you're storing on S3:

```scala
import scube._

val digest:Future[Array[Byte]] = for {
  Some(bucket) <- S3("cats")
  Some(file) <- bucket("laser-cats.mp4")
} yield using(file.source) { source =>
  import java.security.MessageDigest
  val md = MessageDigest getInstance "MD5"
  source.foreach(md update _.toByte)
  md.digest
}
```

#### Errors?

If you want to catch and handle odd-ball errors (like a `500`), then you might do something like the following:

```scala
import java.io.File
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

val picture = new File("feline.jpg")

val item:Future[Try[FileItem]] = {
  for {
    Some(bucket) <- S3("cats")
    file <- bucket.put("feline.jpg")(picture)
  } yield Success(file)
} recover { case e => Failure(e) }
```
