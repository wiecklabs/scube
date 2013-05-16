package scube

class CredentialsSpec extends test.Spec {
  "Credentials should" - {
    "take a key and a secret" in {
      Credentials("bob", "secret") match {
        case Credentials(key, secret, session, expiration) => {
          key must equal("bob")
          secret must equal("secret")
          session must be(None)
          expiration must be(None)
        }
        case _ => fail
      }
    }
  }
}