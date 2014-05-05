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
import scala.concurrent.Future

object LayoutView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(parentContext:RenderingContext, template:(LayoutRenderingContext) => HtmlFormat.Appendable)

  /**
   * This is used for define concrete instance of the view ( for example inside of the template )
   *
   * @param template
   * @return
   */
  def apply(template:(LayoutRenderingContext) => HtmlFormat.Appendable): ViewRenderer = {

    new ViewRenderer {

      override def apply(context: RenderingContext): Future[Any] = {

        val view = context.createActor()
        render(view, context, template)
      }
    }
  }

  def render(widget:ActorRef, parentContext:RenderingContext, template:(LayoutRenderingContext) => HtmlFormat.Appendable) = widget ? Render(parentContext, template)

  /**
   * Used inside of the template or in the controller
   * TODO: VERY IMPORTANT - VIEW COMPANION OBJECT IS RESPONSIBLE TO PROVIDE ActorRef ( WILL BE ADDED TO THE  RenderingContext )
   *
   * @param viewID      ID of the view. TestView companion object is responsible to process this id and resolve to the ActorRef.
   * @param parentContext
   * @param template
   */
  // def render(viewID:String,
  //           parentContext:RenderingContext,
  //           template:(String, RenderingContext) => HtmlFormat.Appendable,
  //           renderingContext:Option[ActorRenderingContext] = None
  //          ) = {

    // TODO: VERY IMPORTANT !! WHEN CONSTRCTING PARENT VIEW WE CAN SPECIFY HOW CHILD VIEWS CAN BE CONSTRUCTUED
    // TODO: AS WELL AS THEY SCOPE !!!

    // TODO: We can specify context class and scope as additional optional parameters to render function
    // TODO: Usage of the custom context classes will allow to customize view resolving and scopes management

    // val context = new ActorRenderingContext(Some(parentContext))

    // TODO: How to deal with:
    // TODO: - Scopes ( request, sessions, user, site wide )
    // TODO: - Different view actor creation techniques ( in the controller and in the parent actor )
    // var view:ActorRef = Akka.system.actorOf(Props[TemplateWidget], name = "viewID")

    // var view:ActorRef = parentContext.resolveView(viewID)
  // }
}

/**
 * Render template
 */
class LayoutView extends Actor {

  override def receive: Receive = {

    case Render(parentContext, template) => render(parentContext, template)

  }

  private def render(parentContext:RenderingContext, template:(LayoutRenderingContext) => HtmlFormat.Appendable):HtmlFormat.Appendable = {

    val renderingContext = new LayoutRenderingContext(Some(parentContext))
    val html= template(renderingContext)

    val body = html.body

    HtmlFormat.raw(body)
  }
}

class LayoutRenderingContext(parentContext:Option[RenderingContext], viewActor:ActorRef) extends ActorRenderingContext(parentContext, viewActor) {

  var views:Map[String, ViewRenderer] = null

  var pendingRenders:Map[String, Future[HtmlFormat.Appendable]] = Map[String, Future[HtmlFormat.Appendable]]()

  def pendingRender(viewID:String, future: Future[HtmlFormat.Appendable]) {
    pendingRenders = pendingRenders+ ((viewID, future))
  }

  def views(views: Map[String, ViewRenderer]) = {
    this.views = views
  }

  /**
   * Will be called from template at the place where view needs to be rendered.
   * Call view renderer and return temporary placeholder for view content.
   * Placeholder will be later be replaced by actual rendered view content.
   *
   * @param viewID    Id of the which is needs to be rendered
   * @return
   */
  def view(viewID:String):String = {

    views.get(viewID) match {

      case Some(viewRenderer) =>
        val future = viewRenderer(this)
        pendingRender(viewID, future)

        s"%%WIDGET_PLACEHOLDER_$viewID%%"

      case None => throw new Exception(s"View $viewID is not defined for template.")
    }
  }
}
