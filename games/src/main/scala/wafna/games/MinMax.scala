package wafna.games
import scala.annotation.tailrec
import scala.language.implicitConversions
object MinMax {
  type Eval = Either[Option[Token], Int]
  def compareEvaluations(currentPlayer: Token, u: Eval, v: Eval): Int = {
    (u, v) match {
      case (Right(u1), Right(u2)) =>
        u1 compareTo u2
      case (Left(u1), Right(v1)) =>
        u1 match {
          case None => 0 compareTo v1
          case Some(p) => if (p == currentPlayer) 1 else -1
        }
      case (Right(u1), Left(v1)) =>
        v1 match {
          case None => u1 compareTo 0
          case Some(p) => if (p == currentPlayer) -1 else 0
        }
      case (Left(u1), Left(v1)) =>
        (u1, v1) match {
          case (None, None) => 0
          case (None, Some(p)) => if (p == currentPlayer) -1 else 1
          case (Some(p), None) => if (p == currentPlayer) 1 else -1
          case (Some(p), Some(q)) => if (p == q) 0 else if (p == currentPlayer) 1 else -1
        }
    }
  }
}
/**
  * Implement min max for a game.
  *
  * @tparam G The type of the game.
  */
abstract class MinMax[G <: Game] {
  import MinMax._
  var searches = 0
  var evaluations = 0
  var evaluationsDepth = 0
  var prunes = 0
  var prunesDepth = 0
  var gamesPruned = 0
  var gamesPrunedDepth = 0
  var elapsed = 0L
  /**
    * Evaluate the game relative to the player.
    *
    * @return a Left indicating the game is complete or a Right giving the value of the board.
    *         The reason for this is that search needs to have explicit knowledge about end of game states.
    */
  def evaluate(player: Token, game: G): Eval
  /**
    * Use the min-max algorithm with the evaluator to select a move.
    */
  def search(maxDepth: Int)(topNode: G): Option[(G, Any)] = {
    // We need a constant reference to the player that is doing the searching so we can
    // evaluate the games consistently.
    val currentPlayer: Token = topNode.turnInHand
    def _search(currentDepth: Int, searchNode: G, prune: Option[Eval]): Option[((G, Any), Eval)] = {
      def eval(game: G): Eval = {
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
            @tailrec def checkOutTheMoves(moves: List[(G, Any)], best: Option[((G, Any), Eval)]): Option[((G, Any), Eval)] = {
              // see if the current player has a win; we can then clearly stop looking.
              if (best.exists(_._2 match {
                case Left(r) => r.contains(searchNode.turnInHand)
                case Right(_) => false
              })) {
                best
              } else {
                moves match {
                  case Nil =>
                    best
                  case nextMove :: nextMoves =>
                    val nextMoveEval: Eval =
                      if (currentDepth < maxDepth)
                      // search downward using our current choice as the pruning value
                        _search(currentDepth + 1, nextMove._1, best.map(_._2)) match {
                          case None =>
                            eval(nextMove._1)
                          case Some(e) => e._2
                        }
                      else {
                        eval(nextMove._1)
                      }
                    //lazy val cp = currentPlayer == searchNode.turnInHand
                    lazy val nextBest: Option[((G, Any), Eval)] = best match {
                      case None =>
                        Some((nextMove, nextMoveEval))
                      case Some((_, currentChoiceEval)) =>
                        if (0 > compareEvaluations(currentPlayer, currentChoiceEval, nextMoveEval))
                          Some((nextMove, nextMoveEval))
                        else best
                    }
                    prune match {
                      case None =>
                        // If there is no pruning value we have to look at everything.
                        checkOutTheMoves(nextMoves, nextBest)
                      case Some(pruner) =>
                        if (0 > compareEvaluations(currentPlayer, pruner, nextBest.get._2)) {
                          prunes += 1
                          prunesDepth += currentDepth
                          gamesPruned += nextMoves.length
                          gamesPrunedDepth += nextMoves.length * currentDepth
                          best
                        }
                        else checkOutTheMoves(nextMoves, nextBest)
                    }
                }
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
