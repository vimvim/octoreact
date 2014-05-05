package widgets

/**
 * View resolver for scope
 */
class ViewResolver(scopeID:ScopeID, viewFactory: ViewFactory) {

  def resolve(scopesHolder:ScopesHolder, viewID:String):ViewRenderer = {

    val scope = scopesHolder.getScope(scopeID)
    scope.resolve(viewID) match {

      case Some(viewRenderer) => viewRenderer

      case None =>

        val viewRenderer = viewFactory(viewID)
        scope.register(viewID, viewRenderer)

        viewRenderer
    }
  }
}
