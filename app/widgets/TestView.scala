package widgets

import scala.concurrent.duration._

import akka.actor.{Actor, Props, ActorRef}
import akka.pattern.ask
import akka.util.Timeout

import play.api.templates.HtmlFormat
import scala.concurrent.Future
import widgets.TestView.Render

/**
 * Interface to the TestView.
 * TestView is used for testing various rendering scenarios and can be bound to the Request, Session, Site scopes.
 * This is most likely will not be occurs for ordinal widgets used in the system.
 *
 * Test view is display countdown as well as some view identification.
 *
 */
object TestView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(parentContext: RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable)

  /**
   * This is used for define concrete instance of the view ( for example inside of the template )
   * Will return view renderer function which will works like are interface for rendering view.
   *
   * @param template
   * @return
   */
  def apply(template: (RenderingContext) => HtmlFormat.Appendable): ViewFactory = {

    new ViewFactory() {

      /**
       * This method will be called for resolving concrete instance of the view ( for example from scope handler )
       *
       * @return
       */
      override def getInstance(parentContext:RenderingContext): ViewRenderer = {

        val view = parentContext.createActor(Props[TestView])

        new ViewRenderer(){

          override def apply(): Future[RenderResponse] =
            (view ? Render(parentContext, template)).mapTo
        }
      }
    }
  }

  def render(widget:ActorRef, parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable) =
    widget ? Render(parentContext, template)

}

/**
 * Test view. Used for testing interaction with the view actor.
 */
class TestView(viewID:String) extends Actor {

  override def receive: Receive = {

    case Render(parentContext, template) => sender ! render(parentContext, template)

  }

  private def render(parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable):RenderResponse = {

    val renderingContext = new ActorRenderingContext(Some(parentContext), self)
    val html= template(renderingContext)

    val body = html.body

    RenderedContent(viewID, body)

    // HtmlFormat.raw(body)
  }
}
