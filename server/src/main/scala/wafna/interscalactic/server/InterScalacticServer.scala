package wafna.interscalactic.server
import java.io.File
import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import wafna.interscalactic.db.InterScalacticDB
import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
  * THE APP.
  */
object InterScalacticServer extends App {
  private val appConfig = ConfigFactory.defaultApplication()
  private val hostConfig = appConfig.withFallback(ConfigFactory.empty().
      withValue("host.interface", ConfigValueFactory fromAnyRef "localhost").
      withValue("host.web-dir", ConfigValueFactory fromAnyRef "")) getConfig "host"
  implicit val actorSystem: ActorSystem = ActorSystem("InterScalacticServer")
  private lazy val log = Logging(actorSystem, InterScalacticServer.getClass)
  InterScalacticDB.withDatabase(3 * Runtime.getRuntime.availableProcessors()) { db =>
    implicit val executionContext: MessageDispatcher = actorSystem.dispatchers.lookup("http-dispatcher")
    implicit val webDir: Option[File] = {
      val dir = hostConfig getString "web-dir"
      if (dir.nonEmpty) Some(new File(dir))
      else None
    }
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val rejectionHandler = corsRejectionHandler withFallback RejectionHandler.default
    val exceptionHandler = ExceptionHandler {
      case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
    }
    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)
    lazy val routes: Route = handleErrors {
      cors() {
        handleErrors {
          pathPrefix("api")(APIRoutes.router(db, actorSystem, executionContext)) ~
              webDir.map(StaticContent.route(actorSystem, _)).getOrElse(reject)
        }
      }
    }
    val interface = hostConfig getString "interface"
    val port = hostConfig getInt "port"
    Http().bindAndHandle(routes, interface, port)
    log.info(s"Server online at http://$interface:$port/")
    Await.result(actorSystem.whenTerminated, Duration.Inf)
    log.warning("--- HTTP exit")
  }
  log.warning("--- APP exit")
}
