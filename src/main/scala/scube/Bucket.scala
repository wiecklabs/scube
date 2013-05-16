package scube

case class Bucket(name: String,
                  delimiter: Option[String] = Some("/"))