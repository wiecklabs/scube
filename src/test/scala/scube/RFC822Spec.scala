package scube

import org.joda.time.DateTime

class RFC822Spec extends test.Spec {
  "A Date should" - {
    "be formatted as RFC822" in {
      val date = new DateTime(2013, 5, 16, 11, 2, 30)
      RFC822(date.toDate) must equal("Thu, 16 May 2013 16:02:30 UTC")
    }
  }

}
