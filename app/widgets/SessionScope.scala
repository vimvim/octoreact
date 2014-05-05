package widgets

object SessionScope {

  object ID extends ScopeID("Session")

  def apply(viewFactory: ViewFactory) = new ViewResolver(ID, viewFactory)
}
