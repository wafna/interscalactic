package wafna.games
import org.scalatest.FlatSpec
import wafna.games.Hexapawn.MinMaxHP
import wafna.games.Token.{P1, P2}
import RunHexapawn._
class TestHexapawn extends FlatSpec {
  def printMoves(moves: List[(Hexapawn, Any)]): Unit =
    println(moves.map(m => s"${m._1.player.show} ${m._2}\n${m._1.show}").mkString("\n---\n"))
  "indices" should "have length 3" in {
    assert(3 == Hexapawn.indices.length)
  }
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
        // printMoves(turn2)
        turn2.head._1.result match {
          case Left(e) =>
            fail(s"Over so soon? $e")
          case Right(turn3) =>
            assert(3 == turn3.length)
          // printMoves(turn3)
        }
    }
  }
  "MinMaxHP" should "evaluate correctly" in {
    /**
      * @param path seq of (which move to pick, expected evaluation).
      */
    def runChain(path: Seq[(Int, Int)]): Unit = {
      val start = newGame
      val starter = newGame.player
      assert(0 == MinMaxHP.evaluate(P1, start))
      def _runChain(game: Hexapawn, path: Seq[(Int, Int)]): Unit = {
        // println(s"-- [${path.length}] ${game.player.show} [eval ${MinMaxHP.evaluate(starter, game)}]\n${game.show}")
        lazy val result = game.result
        path match {
          case Nil =>
          /*
                      println("Path is empty.")
                      result match {
                        case Left(e) => println(e)
                        case Right(moves) =>
                          println(s"Remaining moves:")
                          moves.zipWithIndex foreach { case (move, nth) =>
                            println(s"[$nth] - ${move._1.player.show} ${move._2}\n${move._1.show}")
                          }
                      }
                      println("done")
          */
          case (pick, eval) :: ps =>
            result match {
              case Left(e) => println(s"GAME OVER $e")
              case Right(moves) =>
                val move = moves.drop(pick).head // better be there!
              val evaluation = MinMaxHP.evaluate(starter, move._1)
                assert(evaluation == eval)
                _runChain(move._1, ps)
            }
        }
      }
      _runChain(start, path)
    }
    runChain(List((1, 0), (2, 0), (0, 0), (0, 0), (1, 1000)))
  }
}