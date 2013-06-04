package scube

import scala.xml._
import org.joda.time.DateTime

class LifecycleDeserializationSpec extends test.Spec {

  val bucket = Bucket("bob")

  val date = new DateTime(2013, 5, 16, 11, 2, 30).toDate

  val sampleRules = Seq(
    Rule("archive", "glacier/").transitions(30),
    Rule("tmp", "tmp/").expires(1))

  "An Expiration should" - {
    "not match" in {
      val xml = <Bob></Bob>

      Expiration.unapply(xml) must equal(None)
    }

    "deserialize with days" in {
      val xml = <Expiration><Days>42</Days></Expiration>

      Expiration.unapply(xml) must equal(Some(DaysExpiration(42)))
    }

    "deserialize with date" in {
      val xml = <Expiration><Date>Thu, 16 May 2013 16:02:30 UTC</Date></Expiration>

      Expiration.unapply(xml) must equal(Some(DateExpiration(date)))
    }
  }

  "A Transition should" - {
    "not match" in {
      val xml = <Bob></Bob>

      Transition.unapply(xml) must equal(None)
    }

    "deserialize with days" in {
      val xml = <Transition><Days>42</Days><StorageClass>GLACIER</StorageClass></Transition>

      Transition.unapply(xml) must equal(Some(DaysTransition(42)))
    }

    "deserialize with date" in {
      val xml = <Transition><Date>Thu, 16 May 2013 16:02:30 UTC</Date><StorageClass>GLACIER</StorageClass></Transition>

      Transition.unapply(xml) must equal(Some(DateTransition(date)))
    }
  }

  "A Rule should" - {
    "not match" in {
      val xml = <Bob></Bob>

      Rule.unapply(xml) must equal(None)
    }

    "not match even with an ID" in {
      val xml = <Rule><ID>Bob</ID></Rule>

      Rule.unapply(xml) must equal(None)
    }

    "match with an Expiration" in {
      val xml = <Rule>
        <ID>tmp</ID>
        <Prefix>tmp/</Prefix>
        <Status>Enabled</Status>
        <Expiration>
          <Days>1</Days>
        </Expiration>
      </Rule>

      Rule.unapply(xml) must equal(Some(sampleRules.last))
    }

    "match with a Dated Expiration" in {
      val xml = <Rule>
        <ID>tmp</ID>
        <Prefix>tmp/</Prefix>
        <Status>Enabled</Status>
        <Expiration>
          <Date>Thu, 16 May 2013 16:02:30 UTC</Date>
        </Expiration>
      </Rule>

      Rule.unapply(xml) must equal(Some(sampleRules.last.expires(date)))
    }

    "match with a Transition" in {
      val xml = <Rule>
        <ID>archive</ID>
        <Prefix>glacier/</Prefix>
        <Status>Enabled</Status>
        <Transition>
          <Days>30</Days>
        </Transition>
      </Rule>

      Rule.unapply(xml) must equal(Some(sampleRules.head))
    }

    "match with both a Transition and an Expiration" in {
      val xml = <Rule>
        <ID>archive</ID>
        <Prefix>glacier/</Prefix>
        <Status>Enabled</Status>
        <Transition>
          <Days>30</Days>
        </Transition>
        <Expiration>
          <Date>Thu, 16 May 2013 16:02:30 UTC</Date>
        </Expiration>
      </Rule>

      Rule.unapply(xml) must equal(Some(sampleRules.head.expires(date)))
    }

    "order of Transition and Expiration doesn't matter" in {
      val xml = <Rule>
        <ID>archive</ID>
        <Prefix>glacier/</Prefix>
        <Status>Enabled</Status>
        <Expiration>
          <Date>Thu, 16 May 2013 16:02:30 UTC</Date>
        </Expiration>
        <Transition>
          <Days>30</Days>
        </Transition>
      </Rule>

      Rule.unapply(xml) must equal(Some(sampleRules.head.expires(date)))
    }
  }

  "Rules should" - {

    val xml = <LifecycleConfiguration>
      <Rule>
        <ID>archive</ID>
        <Prefix>glacier/</Prefix>
        <Status>Enabled</Status>
        <Transition>
          <Days>30</Days>
          <StorageClass>GLACIER</StorageClass>
        </Transition>
      </Rule>
      <Rule>
        <ID>tmp</ID>
        <Prefix>tmp/</Prefix>
        <Status>Enabled</Status>
        <Expiration>
          <Days>1</Days>
        </Expiration>
      </Rule>
    </LifecycleConfiguration>

    "fail to deserialize" in {
      val rules = xml \\ "Rule" ++ <Rule>BOB!</Rule>

      Rules.unapplySeq(rules) must equal(None)
    }

    "deserialize with one rule" in {
      val rules = xml \\ "Rule" head

      Rules.unapplySeq(rules) must equal(Some(Seq(sampleRules.head)))
    }

    "deserialize multiple rules" in {
      val rules = xml \\ "Rule"

      Rules.unapplySeq(rules) must equal(Some(sampleRules))
    }
  }

  "A Lifecycle should" - {

    val lifecycle = Lifecycle.empty(bucket)

    "fail to deserialize" in {
      val xml = <LifecycleConfiguration></LifecycleConfiguration>

      lifecycle.unapply(xml) must equal(None)
    }

    "fail to deserialize with an invalid Rule present" in {
      val xml = <LifecycleConfiguration>
        <Rule>One</Rule>
        <Rule>Two</Rule>
      </LifecycleConfiguration>

      lifecycle.unapply(xml) must equal(None)
    }

    "deserialize with a single rule" in {
      val xml = <LifecycleConfiguration>
        <Rule>
          <ID>archive</ID>
          <Prefix>glacier/</Prefix>
          <Status>Enabled</Status>
          <Transition>
            <Days>30</Days>
            <StorageClass>GLACIER</StorageClass>
          </Transition>
        </Rule>
      </LifecycleConfiguration>

      val expectation = Some(lifecycle ++ sampleRules.head)

      lifecycle.unapply(xml) must equal(expectation)
    }

    "deserialize with multiple rules" in {
      println("BEGIN MULTI")
      val xml = <LifecycleConfiguration>
        <Rule>
          <ID>archive</ID>
          <Prefix>glacier/</Prefix>
          <Status>Enabled</Status>
          <Transition>
            <Days>30</Days>
            <StorageClass>GLACIER</StorageClass>
          </Transition>
        </Rule>
        <Rule>
          <ID>tmp</ID>
          <Prefix>tmp/</Prefix>
          <Status>Enabled</Status>
          <Expiration>
            <Days>1</Days>
          </Expiration>
        </Rule>
      </LifecycleConfiguration>

      val expectation = Some(lifecycle ++ sampleRules)

      lifecycle.unapply(xml) must equal(expectation)
      println("END MULTI")
    }
  }
}
