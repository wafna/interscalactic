package wafna.interscalactic.db
import java.util.concurrent.Executors
import slick.dbio.Effect
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.sql._
import wafna.interscalactic.domain
import wafna.interscalactic.domain.{Role, User}
import wafna.interscalactic.Util.AutoCloseBracket
import wafna.interscalactic.db.InterScalacticSchema._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
/**
  * Wrapper around the database providing an API.
  */
class InterScalacticDB private(db: H2Profile.backend.Database) {
  import wafna.interscalactic.db.InterScalacticDB._
  object user {
    def list: Future[Seq[User]] = db.run(users.result)
    def byIds(ids: Seq[Int]): Future[Seq[User]] = db.run(schema.user.byIds(ids).result)
    def create(givenName: String, familyName: String): Future[User] = db.run(schema.user.create(givenName, familyName))
    def delete(ids: Seq[Int]): Future[Int] = db.run(schema.user.byIds(ids).delete)
    def update(user: User): Future[Int] = db.run(schema.user.update(user))
  }
  object role {
    def list: Future[Seq[Role]] = db.run(roles.result)
  }
  object userRole {
    def list: Future[Seq[(String, String, String)]] = db.run(schema.userRole.list.result)
  }
}
/**
  * Operations for the DB to run.
  */
object InterScalacticDB {
  object schema {
    object user {
      def byIds(ids: Seq[Int]): Query[Users, domain.User, Seq] = for (u <- users if u.id inSet ids) yield u
      // How to get last inserted id.
      def create(givenName: String, familyName: String): FixedSqlAction[User, NoStream, Effect.Write] =
        (users returning users.map(_.id) into ((user, id) => user.copy(id = id))) += User(0, givenName, familyName)
      def update(user: User): FixedSqlAction[Int, NoStream, Effect.Write] = users.filter(_.id === user.id).update(user)
    }
    object role {}
    object userRole {
      val list: Query[(Rep[String], Rep[String], Rep[String]), (String, String, String), Seq] = for {
        u <- users
        r <- roles
        ur <- usersRoles if u.id === ur.userId && r.id === ur.roleId
      } yield (u.givenName, u.familyName, r.code)
    }
  }
  /**
    * Loaner of a database. Creates the execution context.
    * @param threadCount Number of threads in the pool.
    * @param borrower Database exists until borrower returns.
    * @tparam T Whatever you want.
    * @return
    */
  def withDatabase[T](threadCount: Int)(borrower: InterScalacticDB => T): T = {
    implicit val executionContent: ExecutionContextExecutor =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(3 * Runtime.getRuntime.availableProcessors()))
    Database.forConfig("h2mem") autoClose { db =>
      // Dummy data...
      Await.result(
        db run DBIO.seq(
          (users.schema ++ roles.schema ++ usersRoles.schema).create,
          roles ++= Seq(Role(0, "admin"), Role(0, "reader"), Role(0, "writer")),
          users ++= Seq(
            User(0, "Isaac", "Newton"),
            User(0, "Albert", "Einstein"),
            User(0, "James", "Clerk Maxwell"),
            User(0, "Erwin", "Schrödinger")
          ),
          usersRoles ++= Seq((1, 1), (2, 2), (2, 3))
        ),
        2.seconds
      )
      borrower(new InterScalacticDB(db))
    }
  }
}
