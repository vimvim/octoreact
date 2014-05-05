package widgets

import akka.actor.ActorRef

/**
 * Created by vim on 4/30/14.
 */
abstract class RenderingContext(val parentContext:Option[RenderingContext]) {

}

class ControllerRenderingContext extends RenderingContext(None) {

}

class ActorRenderingContext(parentContext:Option[RenderingContext]) extends RenderingContext(parentContext) {

}
