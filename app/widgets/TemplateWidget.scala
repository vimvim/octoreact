package widgets

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
 * Render template
 */
class TemplateWidget extends Actor {

  override def receive: Receive = {

    case Render(templateInfo) => // render(templateInfo)

  }

  /*
  private def render(templateInfo:TemplateInfo):play.api.templates.HtmlFormat.Appendable = {
    templateInfo.render()


  }
  */
}
