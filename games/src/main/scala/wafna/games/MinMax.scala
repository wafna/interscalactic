package wafna.games
import scala.annotation.tailrec
import scala.language.implicitConversions
/**
  * Implement min max for a game.
  *
  * @tparam G The type of the game.
  */
abstract class MinMax[G <: Game] {
  /**
    * Evaluate the game relative to the player.
    */
  def evaluate(player: Token, game: G): Int
  /**
    * Use the min-max algorithm with the evaluator to select a move.
    */
  def search(maxDepth: Int) /*(sortMoves: (Token, List[(G, Any)]) => List[(G, Any)])*/ (topNode: G): Option[(G, Any)] = {
    var searches = 0
    var evaluations = 0
    var evaluationsDepth = 0
    var prunes = 0
    var prunesDepth = 0
    var gamesPruned = 0
    var gamesPrunedDepth = 0
    var elapsed = 0L
    // We need a constant reference to the player that is doing the searching so we can
    // evaluate the games consistently.
    val currentPlayer: Token = topNode.turnInHand
    def _search(currentDepth: Int, searchNode: G, prune: Option[Int]): Option[((G, Any), Int)] = {
      def eval(game: G): Int = {
        evaluations += 1
        evaluationsDepth += currentDepth
        evaluate(currentPlayer, game)
      }
      searchNode.result match {
        case Left(e) =>
          sys error s"Game is already decided: $e"
        case Right(availableMoves) =>
          searches += 1
          if (availableMoves.isEmpty)
            None
          else {
            // examine each move updating our choice and stopping on a prune.
            @tailrec def checkOutTheMoves(moves: List[(G, Any)], best: Option[((G, Any), Int)]): Option[((G, Any), Int)] = moves match {
              case Nil =>
                best
              case nextMove :: nextMoves =>
                val nextMoveEval: Int =
                  if (currentDepth < maxDepth)
                  // search downward using our current choice as the pruning value
                    _search(currentDepth + 1, nextMove._1, best.map(_._2)) match {
                      case None =>
                        eval(nextMove._1)
                      case Some(e) => e._2
                    }
                  else {
                    eval(nextMove.asInstanceOf[G])
                  }
                lazy val cp = currentPlayer == searchNode.turnInHand
                lazy val nextBest: Option[((G, Any), Int)] = best match {
                  case None =>
                    Some((nextMove, nextMoveEval))
                  case Some((_, currentChoiceEval)) =>
                    val dx = currentChoiceEval - nextMoveEval
                    if (cp && 0 > dx || !cp && 0 < dx)
                      Some((nextMove, nextMoveEval))
                    else best
                }
                prune match {
                  case None =>
                    // If there is no pruning value we have to look at everything.
                    checkOutTheMoves(nextMoves, nextBest)
                  case Some(pruner) =>
                    val dx = pruner - nextBest.get._2
                    if (cp && 0 <= dx || !cp && 0 >= dx) {
                      prunes += 1
                      prunesDepth += currentDepth
                      gamesPruned += nextMoves.length
                      gamesPrunedDepth += nextMoves.length * currentDepth
                      best
                    }
                    else checkOutTheMoves(nextMoves, nextBest)
                }
            }
            checkOutTheMoves(availableMoves.map(p => (p._1.asInstanceOf[G], p._2)), None)
          }
      }
    }
    val startTime = System.currentTimeMillis()
    try _search(0, topNode, None).map(_._1)
    finally elapsed += System.currentTimeMillis() - startTime
  }
  /*
    def printStats(): Unit = {
      def suite(v: Double, vd: Double): String =
        f"${v / turns.toDouble / maxDepth
        }%.2f   ${
          vd / v / maxDepth
        }%.3f "
      println(
        f"""             $name
           |       turns $turns
           |    searches ${
          suite(searches, searchesDepth)
        }
           | evaluations ${
          suite(evaluations, evaluationsDepth)
        }
           |      prunes ${
          suite(prunes, prunesDepth)
        }
           |games pruned ${
          suite(gamesPruned, gamesPrunedDepth)
        }
           |     elapsed ${
          elapsed.toDouble / turns
        }%.2f""".stripMargin)
    }
  */
}
