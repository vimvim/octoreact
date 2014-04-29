
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

/**
 * Some tests for
 */
class ViewsRenderingSpec extends Specification {

  "Application" should {

    "be able to send rendering request to the widget with the template which is not have any parameters" in {
      val a = views.html.test1() _
      val b = 1

      // TODO: Some idea - We needs to have separated Render messages for every view type.
      // TODO: Template parameter passed to Render message is Function0-FunctionN type ( depends on the
      // TODO: count of the params which is view needs pass to template )
      // TODO: Return type for function HtmlAppendable ( will check )

      // TODO: Will try to send Render message to the LayoutWidget

      ok
    }

    "be able to send rendering request to the widget with the template which is usage params and no view specific params" in {
      val a = views.html.test2("red") _
      val b = 1

      ok
    }

    "be able to send rendering request to the widget with the template which have view specific params and no usage params" in {
      val a = views.html.test3() _
      val b = 1

      ok
    }

    "be able to send rendering request to the widget with the template which have view specific params and usage params" in {
      val a = views.html.test4("red") _
      val b = 1

      ok
    }
  }
}
