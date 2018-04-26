package wafna.interscalactic.db
import slick.jdbc.H2Profile.api._
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }
import wafna.interscalactic.domain.{ Role, User }
object InterScalacticSchema {
  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def givenName: Rep[String] = column[String]("name")
    def familyName: Rep[String] = column[String]("email")
    def * : ProvenShape[User] = (id, givenName, familyName) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]
  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def code: Rep[String] = column[String]("code")
    def * : ProvenShape[Role] = (id, code) <> (Role.tupled, Role.unapply)
  }
  val roles = TableQuery[Roles]
  class UsersRoles(tag: Tag) extends Table[(Int, Int)](tag, "users_roles") {
    def userId: Rep[Int] = column[Int]("user_id")
    def roleId: Rep[Int] = column[Int]("role_id")
    def * : ProvenShape[(Int, Int)] = (userId, roleId)
    def user: ForeignKeyQuery[Users, User] = foreignKey("user_fk", userId, users)(_.id)
    def role: ForeignKeyQuery[Roles, Role] = foreignKey("role_fk", roleId, roles)(_.id)
  }
  val usersRoles = TableQuery[UsersRoles]
}
