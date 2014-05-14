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
import scala.Right

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

          override def apply(): Future[RenderResponse] =
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

sealed trait BodyChunk

case class PlaceHolderChunk(viewID:String) extends BodyChunk

case class StaticChunk(text:String) extends BodyChunk

/**
 * Render template
 */
class LayoutView(viewID:String) extends Actor {

  implicit val ec = Akka.system.dispatcher

  override def receive: Receive = {

    case Render(parentContext, template) =>
      render(parentContext, template) match {
        case Left(renderedContent) => sender ! renderedContent
        case Right(future) => future pipeTo sender
      }
  }

  private def render(parentContext:RenderingContext, template:(RenderingContext) => HtmlFormat.Appendable):Either[RenderedContent, Future[RenderedContent]] = {

    val renderingContext = new ActorRenderingContext(Some(parentContext), self)
    val html= template(renderingContext)

    val body = html.body

    val futures = renderingContext.pendingRenders.map((entry)=>{

      val subViewID = entry._1
      val renderingFuture = entry._2

      @volatile var timedOut = false

      val timeoutCatchFuture = renderingFuture map {response=>

        if (timedOut) {
          // lateDeliveryActor ! response
          // log.debug(s"$subContentLabel Rendered too late. Send for async delivery. ")
        }

        response
      }

      val timeoutFuture = after(FiniteDuration(3, "seconds"), Akka.system.scheduler) {

        Future {
          timedOut = true
          RenderTimeout(subViewID)
        }

        // Future successful RenderTimeout(subContentLabel)
      }

      (Future firstCompletedOf Seq(timeoutCatchFuture, timeoutFuture)).mapTo[RenderResponse]
    })

    if (!futures.isEmpty) {
      // Needs to wait when some child views will be rendered.

      val bodyChunks = splitBody(body)

      val renderingFuture = Future.fold(futures)(Map[String, String]()) {
        (contentMap, response: RenderResponse) =>

          // log.debug(s"$label: Got response for subcontent $response")

          response match {
            case RenderedContent(subViewID, data) => contentMap ++ Map((subViewID, data))
            case RenderTimeout(subViewID) => contentMap ++ Map((subViewID, s"<div class='view-async-container' data-viewid='$subViewID'></div>"))
          }

      } map {
        contentMap =>
          // Render all gathered content ( will render template is the real system here )
          renderFlat(bodyChunks, contentMap)
      }

      Right(renderingFuture)
    } else {
      // All child views is rendered ( or there is no child views at all ).

      Left(RenderedContent(viewID, body))
    }

    // HtmlFormat.raw(body)
  }

  private def renderFlat(bodyChunks:Seq[BodyChunk], subViewsData:Map[String,String]):RenderedContent = {

    // Tracer.log(s"$label: Final rendering")

    val body = bodyChunks.foldLeft("")((bodyContent, chunk) => {

      chunk match {

        case PlaceHolderChunk(subViewID) =>

          subViewsData.get(subViewID) match {
            case Some(subViewData) => bodyContent + subViewData
            case None => bodyContent
          }

        case StaticChunk(staticContent) => bodyContent + staticContent
      }
    })

    RenderedContent(viewID, body)
  }

  /**
   * Split body to the chunks
   *
   * @param body
   * @return
   */
  private def splitBody(body:String):Seq[BodyChunk] = {

    trait TokenProcessor {
      val elements:Seq[BodyChunk]
      def apply(token:String):TokenProcessor
    }

    // Will takes rights side ( after starting token )
    // This part include token info, end token and rest of the template ( up to the next start token )
    case class RightSideProcessor(elements:Seq[BodyChunk]) extends TokenProcessor {

      def apply(token:String):TokenProcessor = {

        val parts = token.split("-%%%}")
        val chunkParams = parts.head.trim

        val paramParts = chunkParams.split(' ')
        val chunkType = paramParts.head.trim
        val chunkData = paramParts.tail.head.trim

        val resultElements = PlaceHolderChunk(chunkData) +: elements

        if (!parts.tail.isEmpty) {
          val rightsText = parts.tail.head
          new LeftSideProcessor(StaticChunk(rightsText) +: resultElements)
        } else {
          new LeftSideProcessor(resultElements)
        }
      }
    }

    // Will takes left part ( from starting tag )
    case class LeftSideProcessor(elements:Seq[BodyChunk]) extends TokenProcessor {
      def apply(token:String):TokenProcessor = RightSideProcessor(StaticChunk(token) +: elements)
    }

    val finalProcessor = body.split("\\{-%%%").foldLeft[TokenProcessor](LeftSideProcessor(List[BodyChunk]()))((processor, token)=>processor(token))
    finalProcessor.elements.reverse
  }
}

