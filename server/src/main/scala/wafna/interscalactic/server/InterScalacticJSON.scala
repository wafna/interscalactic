package wafna.interscalactic.server
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat, _}
import wafna.interscalactic.domain.User
/**
  * Marshalling objects for the JSON API.
  */
object InterScalacticJSON {
  final case class ActionPerformed(description: String)
  final case class Users(users: Seq[User])
  sealed abstract class UserAction extends Product
  object UserAction {
    case class Create(givenName: String, familyName: String) extends UserAction
    case class Update(id: Int, givenName: String, familyName: String) extends UserAction
    case class Delete(id: Int) extends UserAction
  }
}
/**
  * Marshalling for JSON API.
  */
trait InterScalacticJSON extends SprayJsonSupport {
  import DefaultJsonProtocol._
  import InterScalacticJSON._
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat3(User)
  implicit val usersJsonFormat: RootJsonFormat[Users] = new RootJsonFormat[Users] {
    override def write(obj: Users): JsValue = JsArray(obj.users.map(_.toJson).toVector)
    override def read(json: JsValue): Users = Users(json.convertTo[Seq[User]])
  }
  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
  implicit val userActionCreateJSON: RootJsonFormat[UserAction.Create] = jsonFormat2(UserAction.Create)
  implicit val userActionUpdateJSON: RootJsonFormat[UserAction.Update] = jsonFormat3(UserAction.Update)
  implicit val userActionDeleteJSON: RootJsonFormat[UserAction.Delete] = jsonFormat1(UserAction.Delete)
  // How to do a variant record or algebraic data type.
  implicit val userActionJsonFormat: RootJsonFormat[UserAction] = new RootJsonFormat[UserAction] {
    override def read(json: JsValue): UserAction = json.asJsObject.getFields("type") match {
      case Seq(JsString("Create")) => json.convertTo[UserAction.Create]
      case Seq(JsString("Update")) => json.convertTo[UserAction.Update]
      case Seq(JsString("Delete")) => json.convertTo[UserAction.Delete]
    }
    override def write(obj: UserAction): JsValue =
      JsObject((obj match {
        case c: UserAction.Create => c.toJson
        case d: UserAction.Delete => d.toJson
        case u: UserAction.Update => u.toJson
      }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
  }
}
