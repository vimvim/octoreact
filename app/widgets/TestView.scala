package widgets

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import play.api.templates.HtmlFormat

/**
 * Interface to the TestView
 */
object TestView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(template:(String, String) => HtmlFormat.Appendable)

  /**
   * This is used for define concrete instance of the view ( for example inside of the template )
   *
   * @param template
   * @return
   */
  def apply(template:(String, String) => HtmlFormat.Appendable):ViewFactory ={

    new ViewFactory {

      override def apply(viewID:String, parentContext:RenderingContext): ViewRenderer = {

        new ViewRenderer {

          override def apply(context: RenderingContext): String = {


          }
        }
      }
    }
  }

  /**
   * Used for send rendering request to the TestView actor.
   *
   * @param widget
   * @param template
   * @return
   */
  // def render(widget:ActorRef, template:(String, String) => HtmlFormat.Appendable) = widget ? Render(template)

  /**
   * Used inside of the template or in the controller
   * TODO: VERY IMPORTANT - VIEW COMPANION OBJECT IS RESPONSIBLE TO PROVIDE ActorRef ( WILL BE ADDED TO THE  RenderingContext )
   *
   * @param viewID      ID of the view. TestView companion object is responsible to process this id and resolve to the ActorRef.
   * @param context
   * @param template
   */
  // def render(viewID:String, context:RenderingContext, template:(String, String) => HtmlFormat.Appendable) = {
  //
  // }
}

/**
 * Test view. Used for testing interaction with the view actor.
 */
class TestView {

}
