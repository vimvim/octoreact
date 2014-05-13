package widgets.template

import play.api.templates.Template1
import widgets.{ViewFactory, RenderingContext}

object Render {

  /**
   *
   * TODO: viewID can be optional and be able to be generated using context
   *
   * @param context
   * @param viewFactory
   * @param viewIdOption
   * @return
   */
  def apply(context:RenderingContext, viewFactory:ViewFactory, viewIdOption:Option[String] = None) = {

    def getViewID(viewIdOption:Option[String]) = {

      viewIdOption match {
        case Some(viewID) => viewID
        case None => context.createViewId()
      }
    }

    render(context, viewFactory, getViewID(viewIdOption))
  }

  def render(context:RenderingContext, viewFactory:ViewFactory, viewId:String) = {

    val viewInstance = viewFactory.getInstance(context)
    val future = viewInstance()

    context.pendingRender(viewId, future)

    s"%%WIDGET_PLACEHOLDER_$viewId%%"
  }


}

/*
sealed trait TemplateInfo[Result] {
  def render():Result
}

class TemplateInfo0[Result](template:Template0[Result]) extends TemplateInfo[Result] {
  def render():Result = template.render()
}

class TemplateInfo1[A, Result](template:Template1[A, Result], a:A) extends TemplateInfo[Result] {
  def render():Result = template.render(a)
}

class TemplateInfo2[A, B, Result](template:Template2[A, B, Result], a:A, b:B) extends TemplateInfo[Result] {
  def render():Result = template.render(a,b)
}

class TemplateInfo3[A, B, C, Result](template:Template3[A, B, C, Result], a:A, b:B, c:C) extends TemplateInfo[Result] {
  def render():Result = template.render(a,b,c)
}

object TemplateInfo {

  def apply[Result](template:Template0[Result]):TemplateInfo[Result] = {
    new TemplateInfo0(template)
  }

  def apply[A, Result](template:Template1[A, Result], a:A):TemplateInfo[Result] = {
    new TemplateInfo1(template, a)
  }

  def apply[A, B, Result](template:Template2[A, B, Result], a:A, b:B):TemplateInfo[Result] = {
    new TemplateInfo2(template, a, b)
  }

  def apply[A, B, C, Result](template:Template3[A, B, C, Result], a:A, b:B, c:C):TemplateInfo[Result] = {
    new TemplateInfo3(template, a, b, c)
  }
}

case class Render[Result](template:TemplateInfo[Result])

*/