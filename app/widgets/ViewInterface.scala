package widgets

import scala.concurrent.Future
import play.api.templates.HtmlFormat

/**
 * View renderer
 */
abstract class ViewRenderer extends ((RenderingContext) => Future[HtmlFormat.Appendable])

/**
 * View factory (viewID)
 */
abstract class ViewFactory extends ((String) => ViewRenderer)

