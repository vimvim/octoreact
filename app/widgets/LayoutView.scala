package widgets

import scala.concurrent.duration._

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.util.Timeout

import akka.pattern.ask
import akka.actor.Actor.Receive
import akka.util.Timeout
import akka.actor._
import akka.pattern.{ after, ask, pipe }
import akka.util.Timeout

import play.api.templates.HtmlFormat
import widgets.LayoutView.Render
import play.api.libs.concurrent.Akka
import scala.concurrent.Future
import play.api.Play.current

sealed class RenderResponse()

case class RenderedContent(label:String, content:String) extends RenderResponse

case class RenderTimeout(label:String) extends RenderResponse

object LayoutView {

  implicit val timeout = Timeout(3 seconds)

  case class Render(parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable)

  /**
   * This is used for define concrete instance of the view ( for example inside of the template )
   *
   * @param template
   * @return
   */
  def apply(template:(RenderingContext) => HtmlFormat.Appendable): ViewFactory = {

    new ViewFactory() {

      /**
       * This method will be called for resolving concrete instance of the view ( for example from scope handler )
       *
       * @return
       */
      override def getInstance(parentContext:RenderingContext): ViewRenderer = {

        val view = parentContext.createActor(Props[LayoutView])

        new ViewRenderer(){

          override def apply(): Future[HtmlFormat.Appendable] =
            (view ? Render(parentContext, template)).mapTo
        }
      }
    }
  }

  def render(widget:ActorRef, parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable) = widget ? Render(parentContext, template)

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

  implicit val ec = Akka.system.dispatcher

  override def receive: Receive = {

    case Render(parentContext, template) => render(parentContext, template)

  }

  private def render(parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable):HtmlFormat.Appendable = {

    val renderingContext = new ActorRenderingContext(Some(parentContext), self)
    val html= template(renderingContext)

    val body = html.body

    val futures = renderingContext.pendingRenders.map((entry)=>{

      val subContentLabel = entry._1
      val renderingFuture = entry._2

      @volatile var timedOut = false

      (Future firstCompletedOf Seq(
        renderingFuture map {response=>

          if (timedOut) {
            // lateDeliveryActor ! response
            // log.debug(s"$subContentLabel Rendered too late. Send for async delivery. ")
          }

          response
        },
        after(FiniteDuration(3, "seconds"), Akka.system.scheduler) {

          Future {
            timedOut = true
            RenderTimeout(subContentLabel)
          }

          // Future successful RenderTimeout(subContentLabel)
        }
      )).mapTo[RenderResponse]
    })

    Future.fold(futures)(Map[String, String]()) {
      (contentMap, response: RenderResponse) =>

        // log.debug(s"$label: Got response for subcontent $response")

        response match {
          case RenderedContent(subContentLabel, mimeType, data) => contentMap ++ Map((subContentLabel, data))
          case RenderTimeout(subContentLabel) => contentMap ++ Map((subContentLabel, "Content rendering timeout"))
        }

    } map {
      contentMap =>
        // Render all gathered content ( will render template is the real system here )
        renderFlat(label, topContent, contentMap)
    }

    HtmlFormat.raw(body)
  }

  private def renderFlat(label: String, subContentMap:Map[String,String]):RenderedContent = {

    // Tracer.log(s"$label: Final rendering")

    RenderedContent(label, "text", subContentMap.foldLeft("") {
      (data, entry) =>

        val contentLabel = entry._1
        val contentData = entry._2

        data.concat(s"<div id='$contentLabel'>$contentData</div>")
    })
  }
}

