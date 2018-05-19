package wafna.games
import wafna.games.Token.{P1, P2}
object RunHexapawn extends App {
  import Hexapawn._
  def newGame = new Hexapawn(P1)
  implicit class ShowHexapawn(hexapawn: Hexapawn) {
    def show: String = {
      // x goes right, y goes up
      indices.reverse map { y =>
        indices map { x =>
          hexapawn.board.at(x, y).map(_.show).getOrElse(" ")
        } mkString ""
      } mkString "\n"
    }
  }
  implicit class ShowPlayer(player: Token) {
    def show: String = player match {
      case P1 => "X"
      case P2 => "O"
    }
  }
  def runGame(maxDepth: Int, maxTurns: Option[Int]): Unit = {
    println(s"game: {maxDepth: $maxDepth}, maxTurns: $maxTurns")
    def runIt(turn: Int, game: Hexapawn): Unit = {
      if (maxTurns.forall(_ > turn)) {
        MinMaxHP.search(maxDepth)(game) match {
          case None =>
            println("that's it, then.")
            println(s"[$turn] ${game.player.show}\n${game.show}")
          case Some((g, m)) =>
            println(s"[$turn] ${game.player.show} $m\n${g.show}")
            runIt(1 + turn, g)
        }
      } else {
        println(s"turn limit: $maxTurns")
      }
    }
    runIt(0, newGame)
  }
  runGame(2, Some(4))
}
