package widgets

/**
 * Hold scopes
 */
class ScopesHolder(scopes: Map[ScopeID, Scope]) {

  def getScope(scopeID:ScopeID):Scope = {
    scopes.get(scopeID) match {
      case Some(scope) => scope
      case None => throw new Exception(s"Scope ${scopeID.name} is not registered")
    }
  }
}
