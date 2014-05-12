package widgets

object SessionScope {

  object ID extends ScopeID("Session")

  def apply(viewId:String, viewFactory:ViewFactory):ViewFactory = {

    new ViewFactory {

      override def getInstance(parentContext: RenderingContext): ViewRenderer = {

        parentContext.getScopedEntry(ID, viewId) match {

          case Some(viewRenderer) => viewRenderer

          case None =>

            val viewRenderer = viewFactory.getInstance(parentContext)
            parentContext.putScopedEntry(ID, viewId, viewRenderer)

            viewRenderer
        }
      }
    }
  }
}
