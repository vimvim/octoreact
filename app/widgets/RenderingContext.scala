package widgets

import akka.actor.{Actor, Props, ActorRef}

/**
 * Created by vim on 4/30/14.
 */
abstract class RenderingContext(val parentContext:Option[RenderingContext]) {

  def createActor(props:Props):ActorRef

}

class ControllerRenderingContext(scopes:Map[ScopeID,Scope]) extends RenderingContext(None) {

  def createActor(props:Props):ActorRef

}

class ActorRenderingContext(parentContext:Option[RenderingContext], val viewActor:ActorRef) extends RenderingContext(parentContext) {

  def createActor(props:Props):ActorRef = {

  }

}
