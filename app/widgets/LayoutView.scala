package widgets

import scala.concurrent.duration._

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.util.Timeout

import akka.pattern.ask
import akka.actor.Actor.Receive
import akka.util.Timeout

import play.api.templates.HtmlFormat
import widgets.LayoutView.Render
import play.api.libs.concurrent.Akka
import javax.swing.text.ViewFactory

object LayoutView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(template:(String, RenderingContext) => HtmlFormat.Appendable, context:RenderingContext)

  def render(widget:ActorRef, context:RenderingContext, template:(String, RenderingContext) => HtmlFormat.Appendable) = widget ? Render(template, context)

  /**
   * Used inside of the template or in the controller
   * TODO: VERY IMPORTANT - VIEW COMPANION OBJECT IS RESPONSIBLE TO PROVIDE ActorRef ( WILL BE ADDED TO THE  RenderingContext )
   *
   * @param viewID      ID of the view. TestView companion object is responsible to process this id and resolve to the ActorRef.
   * @param parentContext
   * @param template
   */
  def render(viewID:String,
             parentContext:RenderingContext,
             template:(String, RenderingContext) => HtmlFormat.Appendable,
             renderingContext:Option[ActorRenderingContext] = None
            ) = {

    // TODO: VERY IMPORTANT !! WHEN CONSTRCTING PARENT VIEW WE CAN SPECIFY HOW CHILD VIEWS CAN BE CONSTRUCTUED
    // TODO: AS WELL AS THEY SCOPE !!!

    // TODO: We can specify context class and scope as additional optional parameters to render function
    // TODO: Usage of the custom context classes will allow to customize view resolving and scopes management

    val context = new ActorRenderingContext(Some(parentContext))

    // TODO: How to deal with:
    // TODO: - Scopes ( request, sessions, user, site wide )
    // TODO: - Different view actor creation techniques ( in the controller and in the parent actor )
    // var view:ActorRef = Akka.system.actorOf(Props[TemplateWidget], name = "viewID")

    var view:ActorRef = parentContext.resolveView(viewID)


  }
}

/**
 * Render template
 */
class LayoutView(viewId:String) extends Actor {

  override def receive: Receive = {

    case Render(template) => render(template)

  }

  private def render(template:(String, RenderingContext) => HtmlFormat.Appendable):HtmlFormat.Appendable = {

    val renderingContext = new RenderingContext()
    val html= template(viewId, renderingContext)

    val body = html.body

    HtmlFormat.raw(body)
  }
}

class LayoutRenderingContext(val view:ActorRef) {

  def widgets(widgets: Map[String, ViewResolver]) = {

  }

  def widget(viewID:String) = {

  }
}
