package scube

import org.joda.time.DateTime
import scala.xml.Utility

class RuleSpec extends test.Spec {

  "A Rule should" - {
    val rule = Rule("example-rule", "tmp")

    "have" - {
      "an id" in {
        rule.id must equal("example-rule")
      }

      "an optional prefix" in {
        rule.prefix must equal(Some("tmp"))
      }

      "an enabled attribute that defaults to true" in {
        rule.enabled must be(true)
      }
    }

    "allow you to expire it" - {

      "but default to NoExpiration" in {
        rule.expiration must equal(NoExpiration)
      }

      "by number of days" in {
        val expirationRule = rule.expires(1)

        expirationRule.expiration must equal(DaysExpiration(1))
      }

      "require non-zero number of days" in intercept[IllegalArgumentException] {
        rule.expires(0)
      }

      "by date" in {
        val expirationDate = new DateTime(2013, 5, 16, 11, 2, 30).toDate
        val expirationRule = rule.expires(expirationDate)

        expirationRule.expiration must equal(DateExpiration(expirationDate))
      }
    }

    "allow you to transition it" - {

      "but default to NoTransition" in {
        rule.transition must equal(NoTransition)
      }

      "by number of days" in {
        val transitionRule = rule.transitions(1)

        transitionRule.transition must equal(DaysTransition(1))
      }

      "require non-zero number of days" in intercept[IllegalArgumentException] {
        rule.transitions(0)
      }

      "by date" in {
        val transitionDate = new DateTime(2013, 5, 16, 11, 2, 30).toDate
        val transitionRule = rule.transitions(transitionDate)

        transitionRule.transition must equal(DateTransition(transitionDate))
      }
    }

    "serialize to xml" in {
      rule.toXml must equal(Utility.trim(
        <Rule>
          <ID>example-rule</ID>
          <Prefix>tmp</Prefix>
          <Status>Enabled</Status>
        </Rule>))
    }
  }
}