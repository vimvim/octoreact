package widgets

import scala.concurrent.Future
import play.api.templates.HtmlFormat

/*
object ViewRenderer {

  def apply(f:(RenderingContext) => Future[HtmlFormat.Appendable]):ViewRenderer = {

    new ViewRenderer() {
      override def apply(v1: RenderingContext): Future[HtmlFormat.Appendable] = f(v1)
    }
  }
}
*/

/**
 * View renderer.
 * Simple interface for rendering of the view.
 */
abstract class ViewRenderer extends (() => Future[RenderResponse])

/*
object ViewFactory {

  def apply(f:ViewRenderer) = {

    new ViewFactory() {
      override def apply(): ViewRenderer = f
    }
  }
}
*/

/**
 * View factory
 * parentContext -> ViewRenderer
 */
abstract class ViewFactory {
  def getInstance(parentContext:RenderingContext):ViewRenderer
}
