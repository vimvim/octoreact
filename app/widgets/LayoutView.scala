package widgets

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import akka.pattern.ask
import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import akka.util.Timeout

import play.api.templates.HtmlFormat
import widgets.LayoutView.Render

object LayoutView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(template:(String, RenderingContext) => HtmlFormat.Appendable)

  def render(widget:ActorRef, template:(String, RenderingContext) => HtmlFormat.Appendable) = widget ? Render(template)
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
