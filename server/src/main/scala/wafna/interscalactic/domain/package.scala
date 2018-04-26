package wafna.interscalactic

package object domain {
  final case class User(id: Int, givenName: String, familyName: String)
  final case class Role(id: Int, code: String)
}
