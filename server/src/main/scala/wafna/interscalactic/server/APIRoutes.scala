package wafna.interscalactic.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{ get, post }
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import akka.util.Timeout
import wafna.interscalactic.db.InterScalacticDB
import wafna.interscalactic.domain.User

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

/**
  * Entities and serialization for JSON payloads.
  */
object APIRoutes extends InterScalacticJSON {
  import InterScalacticJSON._
  def router(db: InterScalacticDB, system: ActorSystem, executionContext: ExecutionContext): Route = {
    lazy val log = Logging(system, APIRoutes.getClass)
    implicit lazy val timeout: Timeout = Timeout(2.seconds)
    // get for queries, post for mutations.  Simpler semantics
    pathPrefix("users") {
      implicit val ec: ExecutionContext = executionContext
      get {
        pathEnd {
          complete(db.user.list.mapTo[Seq[User]].map(Users))
        } ~ parameter("id".as[Int]) { id =>
          rejectEmptyResponse {
            complete(db.user.byIds(Seq(id)).map(_.headOption))
          }
        }
      } ~ post {
        entity(as[UserAction]) { action =>
          log.info(action.toString)
          action match {
            case UserAction.Create(givenName, familyName) =>
              onComplete((db.user.create(givenName, familyName) map { userId =>
                ActionPerformed(s"User created $userId")
              }).mapTo[ActionPerformed]) {
                case Success(performed) =>
                  complete((StatusCodes.OK, performed))
                case Failure(e) =>
                  complete((StatusCodes.InternalServerError, e.getMessage))
              }
            case UserAction.Update(id, givenName, familyName) =>
              onComplete((db.user.update(User(id, givenName, familyName)) map { userId =>
                ActionPerformed(s"User updated $userId")
              }).mapTo[ActionPerformed]) {
                case Success(performed) =>
                  complete((StatusCodes.OK, performed))
                case Failure(e) =>
                  complete((StatusCodes.InternalServerError, e.getMessage))
              }
            case UserAction.Delete(id) =>
              onSuccess(db.user.delete(List(id)) map { count =>
                ActionPerformed(s"User deleted: $id ($count)")
              }) { performed =>
                complete((StatusCodes.OK, performed))
              }
          }
        }
      }
    }
  }
}
