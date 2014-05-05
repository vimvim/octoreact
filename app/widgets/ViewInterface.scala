package widgets

/**
 * View renderer
 */
abstract class ViewRenderer extends ((RenderingContext) => String)

/**
 * View factory (viewID)
 */
abstract class ViewFactory extends ((String) => ViewRenderer)

