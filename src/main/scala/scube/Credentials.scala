package scube

import java.util.Date

trait Credentials {
  def accessKeyId: String
  def secretKey: String
  def sessionToken: Option[String]
  def expiration: Option[Date]
}

case class AwsCredentials(accessKeyId: String, secretKey: String, sessionToken: Option[String] = None, expiration: Option[Date] = None) extends Credentials

object Credentials extends ((String, String, Option[String], Option[Date]) => Credentials) {

  class AwsConfigurationException extends RuntimeException

  def apply(accessKeyId:String, secretKey:String, sessionToken:Option[String] = None, expiration:Option[Date] = None):Credentials =
    AwsCredentials(accessKeyId, secretKey, sessionToken, expiration)

  def unapply(c:Credentials):Option[(String, String, Option[String], Option[Date])] = c match {
    case null => None
    case _ => Some(c.accessKeyId, c.secretKey, c.sessionToken, c.expiration)
  }

  private lazy val fromConfiguration:Credentials = {
    import com.typesafe.config.{ConfigFactory => TypesafeConfigFactory, Config => TypesafeConfig}

    val config = TypesafeConfigFactory.load
    config.checkValid(TypesafeConfigFactory.defaultReference(), "aws")

    val accessKeyId = config.getString("aws.accessKeyId")
    val secretKey = config.getString("aws.secretKey")

    Credentials(accessKeyId, secretKey)
  }

  implicit def default:Credentials = fromConfiguration
}