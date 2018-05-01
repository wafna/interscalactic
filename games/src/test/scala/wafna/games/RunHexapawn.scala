package wafna.games
import wafna.games.Hexapawn.MinMaxHP
import wafna.games.Token.P1
object RunHexapawn extends App {
  val start = new Hexapawn(P1)
  println(MinMaxHP.evaluate(P1, start))
}
