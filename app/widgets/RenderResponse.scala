package widgets

sealed class RenderResponse()

case class RenderedContent(viewID:String, content:String) extends RenderResponse

case class RenderTimeout(viewID:String) extends RenderResponse

