package widgets


/**
 * TODO: Add locking ?? How to deal with the fact that actors is can be on different nodes ??
 * TODO: THEY CAN'T USE THE SAME SCOPE !!
 */
class Scope {

  var values = Map[String,Any]()

  def get[T](viewID:String):Option[T] = values.get(viewID) match {
    case Some(value) => Some(value.asInstanceOf[T])
    case _ => None
  }

  def register[T](id:String, value:T) = {
    values = values.+((id, value))
  }

}
