package controllers

import play.api.mvc.{Action, Controller}
import akka.actor.{Props, ActorRef}
import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import play.api.libs.concurrent.Akka

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import widgets.{TemplateInfo, Render, TemplateWidget}

object Application extends Controller {

  def index = Action.async {

    val rootWidget:ActorRef = Akka.system.actorOf(Props[TemplateWidget], name = "myactor")
    (rootWidget ? Render(TemplateInfo(views.html.index, "Test application"))) map {
      result =>
        Ok(result)
    }

    /*
    var rootWidget:ActorRef = Akka.system.actorOf(Props[TemplateWidget], name = "myactor")

    (rootWidget ? Render(TemplateInfo(views.html.index, "Test application")).map {
      result =>
        Ok(result)
    }
    */

    /*
    implicit val list = List[Any]()

    val a =  views.html.index
    val b = a("123") _
    val result = b("456")

    Ok(result)
    */

    // Ok(views.html.index("Hello Play Framework"))
  }
}