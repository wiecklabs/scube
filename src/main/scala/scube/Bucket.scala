package scube

import java.util.Date

case class Bucket(name: String,
                  acl:Option[ACL.ACL] = None,
                  createdAt:Option[Date] = None,
                  delimiter: Option[String] = Some("/"))
                 (implicit val credentials:Credentials) {
  override def toString = name
}