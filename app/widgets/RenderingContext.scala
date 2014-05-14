package widgets

import akka.actor.{Actor, Props, ActorRef}
import scala.concurrent.Future
import play.api.templates.HtmlFormat
import play.api.libs.concurrent.Akka
import play.api.Play.current

/**
 * TODO: We have many problems with the scopes. Especially when dealing with the multi-node cluster
 * TODO: Seems that scopes may be not needs at all !! Scopes can be managed transparently by actors and they companion objects
 * TODO: depends on the concrete needs.
 */
abstract class RenderingContext(val parentContext:Option[RenderingContext]) {

  var pendingRenders:Map[String, Future[RenderResponse]] = Map[String, Future[RenderResponse]]()

  def createActor(props:Props):ActorRef

  def getScopedEntry[T](scopeID:ScopeID, viewID:String):Option[T] = None

  def putScopedEntry[T](scopeID:ScopeID, viewID:String, value:T) = {

  }

  // def getScope(scopeID:ScopeID):Scope

  def pendingRender(viewID:String, future: Future[RenderResponse]) {
    pendingRenders = pendingRenders+ ((viewID, future))
  }

  def createViewId():String = "view777"
}

class ControllerRenderingContext(scopes:Map[ScopeID,Scope]) extends RenderingContext(None) {

  def createActor(props:Props):ActorRef = {
    Akka.system.actorOf(props)
  }

}

class ActorRenderingContext(parentContext:Option[RenderingContext], val viewActor:ActorRef) extends RenderingContext(parentContext) {

  def createActor(props:Props):ActorRef = {

    // TODO: How to create child actors. The problem is that we can't use ActorRef ( take into account that parent actor and
    // TODO: childs can be on the different cluster nodes )
    // context.actorOf(props)
    Akka.system.actorOf(props)
  }

}
