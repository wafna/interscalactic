package wafna.games
import org.scalatest.FlatSpec
import wafna.games.Hexapawn.MinMaxHP
import wafna.games.Token.{P1, P2}
object TestHexapawn {
  import Hexapawn._
  implicit class ShowHexapawn(hexapawn: Hexapawn) {
    def show: String = {
      Board.indexer.reverse map { y =>
        Board.indexer map { x =>
          hexapawn.board.at(x, y).map({
            case P1 => "X"
            case P2 => "O"
          }).getOrElse(" ")
        } mkString ""
      } mkString "\n"
    }
  }
}
class TestHexapawn extends FlatSpec {
  def newGame = new Hexapawn(P1)
  import TestHexapawn._
  "board" should "be correctly initially configured" in {
    val board = new Hexapawn.Board()
    for {
      x <- 0 until 3
      y <- 0 until 3
    } yield {
      println(s"${(x, y)}, ${board.at(x, y)}")
    }
    (0 until 3) foreach { x => assert(board.at(x, 0) contains P1) }
    (0 until 3) foreach { x => assert(board.at(x, 1).isEmpty) }
    (0 until 3) foreach { x => assert(board.at(x, 2) contains P2) }
  }
  it should "only allow legal moves" in {
    val start = newGame
    start.result match {
      case Left(e) =>
        fail(s"Over so soon? $e")
      case Right(turn2) =>
        assert(3 == turn2.length)
        println(turn2.map(m => s"${m._2}\n${m._1.show}").mkString("\n---\n"))
        turn2.head._1.result match {
          case Left(e) =>
            fail(s"Over so soon? $e")
          case Right(turn3) =>
            assert(3 == turn3.length)
            println(turn3.map(m => s"${m._2}\n${m._1.show}").mkString("\n---\n"))
        }
    }
  }
  "MinMaxHP" should "evaluate the starting board at 0" in {
    val start = newGame
    assert(0 == MinMaxHP.evaluate(P1, start))
  }
}
