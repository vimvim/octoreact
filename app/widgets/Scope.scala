package widgets

import widgets.ViewRenderer

/**
 * TODO: Add locking
 */
class Scope {

  var views = Map[String,ViewRenderer]()

  def resolve(viewID:String):Option[ViewRenderer] = views.get(viewID)

  def register(viewID:String, viewRenderer:ViewRenderer) = {
    views = views.+((viewID, viewRenderer))
  }

}
