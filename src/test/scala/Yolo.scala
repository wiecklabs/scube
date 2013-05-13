package test

class YoloSpec extends Spec {
	
	implicit class Yolo(s: String) {
		def once = true
	}

  "You" - {
	  "live once" - {
			"you only live".once must be(true)
		}
  }

}