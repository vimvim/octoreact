package widgets

import akka.actor.Actor
import akka.actor.Actor.Receive

object LayoutView {

  def render(template:Function2)

}

/**
 * Render template
 */
class LayoutView extends Actor {

  override def receive: Receive = {

    case Render(templateInfo) => // render(templateInfo)

  }

  /*
  private def render(templateInfo:TemplateInfo):play.api.templates.HtmlFormat.Appendable = {
    templateInfo.render()


  }
  */
}
