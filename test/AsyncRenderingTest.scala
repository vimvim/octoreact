
import scala.concurrent.duration._

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import akka.testkit._

import com.typesafe.config.ConfigFactory

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}

import widgets.{ActorRenderingContext, ControllerRenderingContext, RenderingContext, LayoutView}

/**
 * Created by vim on 5/3/14.
 */
class AsyncRenderingTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll {

  implicit val log = Logging.getLogger(system, this)
  log.info("AsyncRenderingTest started")

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  def this() = this(ActorSystem("AsyncRenderingTest", ConfigFactory.load))

  "An LayoutView" should "be able to correctly render template with the child widgets" in {

    val renderingContext = new ControllerRenderingContext(new RequestScope(), new SessionScope(), new UserScope(), new SiteScope())

    LayoutView.render("root", renderingContext, views.html.test5(), new ActorRenderingContext(renderingContext,
       Map(
        "view1"->SessionScope(Props[TemplateWidget]),
        "view2"->RequestScope(Props[TemplateWidget]),
        "view1"->UserScope(Props[TemplateWidget]),
        "view1"->SiteScope(Props[TemplateWidget]),
       )
    ))
  }
}
