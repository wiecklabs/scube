package scube

object ACL extends Enumeration {
  type ACL = Value

  val PUBLIC_READ = Value("public-read")
  val PUBLIC_READ_WRITE = Value("public-read-write")
  val AUTHENTICATED_READ = Value("authenticated-read")
  val BUCKET_OWNER_READ = Value("bucket-owner-read")
  val BUCKET_OWNER_FULL_CONTROL = Value("bucket-owner-full-control")
}