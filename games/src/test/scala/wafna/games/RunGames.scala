package wafna.games
import wafna.games.Token.{P1, P2}

import scala.annotation.tailrec
import scala.concurrent.duration._
object RunGames {
//  /**
//    * Standard procedure for running a two player game.
//    * @param initialGameState If you take up a game in the middle select the current player as P1.
//    * @param p1 The player whose turn it is.
//    * @param p2 The player following P1.
//    * @tparam G The type of the game
//    * @return An error or a final game state.
//    */
//  def runGame[G <: Game](initialGameState: G)(p1: Player[G], p2: Player[G]): Either[String, G] = {
//    val players = Map[Token, Player[G]](P1 -> p1, P2 -> p2)
//    // If any player is interactive we show game states and whatnot.
//    val interactive = players.values.exists(_.interactive)
//    if (interactive) {
//      players foreach { case (p, b) => println(s"$p -> ${b.name}") }
//    }
//    @tailrec def runLolaRun(gameState: G): Either[String, G] = {
//      // explicit match on gameOver, here, for tailrec.
//      gameState.gameOver match {
//        case None =>
//          players(gameState.currentPlayer).move(gameState) match {
//            case None => Left("invalid game state: (! gameOver) && availableMoves.isEmpty")
//            case Some((nextGameState, move)) =>
//              println(s"move $move")
//              runLolaRun(nextGameState)
//          }
//        case Some(w) =>
//          if (interactive)
//            println(s"RESULT ${w.map(_.toString).getOrElse("Draw")}")
//          Right(gameState)
//      }
//    }
//    runLolaRun(initialGameState)
//  }
//  def head2head[G <: Game](newGame: G, matches: Int, p1: Player[G], p2: Player[G]): Unit = {
//    val t0 = System.currentTimeMillis()
//    println(s"$matches matches of ${p1.name} vs. ${p2.name}")
//    def pad(n: Int)(s: Any): String = (s.toString.length to n).foldLeft(s.toString)((s, _) => " " + s)
//    def printResult(p1: Player[G], p2: Player[G], result: (Int, Int, Int)): Unit = {
//      val total = result._1 + result._2 + result._3
//      println(f"P1   ${pad(4)(result._1)} ${pad(5)(100d * result._1 / total)}%% ${p1.name}")
//      println(f"P2   ${pad(4)(result._2)} ${pad(5)(100d * result._2 / total)}%% ${p2.name}")
//      println(f"DRAW ${pad(4)(result._3)} ${pad(5)(100d * result._3 / total)}%%")
//    }
//    def runGames(p1: Player[G], p2: Player[G]): (Int, Int, Int) = {
//      val result = (1 to matches).foldLeft((0, 0, 0)) { (vs, _) =>
//        Game.runGame(newGame)(p1, p2) match {
//          case Left(e) => QuietException(s"ERROR $e")
//          case Right(g) => g.gameOver match {
//            case None => sys error "wat"
//            case Some(w) => w match {
//              case None => (vs._1, vs._2, vs._3 + 1)
//              case Some(P1) => (vs._1 + 1, vs._2, vs._3)
//              case Some(P2) => (vs._1, vs._2 + 1, vs._3)
//            }
//          }
//        }
//      }
//      printResult(p1, p2, result)
//      result
//    }
//    val r1 = runGames(p1, p2)
//    val r2 = runGames(p2, p1)
//    val result = (r1._1 + r2._2, r1._2 + r2._1, r1._3 + r2._3)
//    val total = result._1 + result._2 + result._3
//    println(s"${pad(5)(100d * result._1 / total)}% ${p1.name}")
//    println(s"${pad(5)(100d * result._2 / total)}% ${p2.name}")
//    println(s"${pad(5)(100d * result._3 / total)}% DRAW")
//    println(s"${pad(5)(100d * (r1._1 + r2._1) / total)}% P1")
//    println(s"${pad(5)(100d * (r1._2 + r2._2) / total)}% P2")
//    val t1 = System.currentTimeMillis()
//    println(s"total time: ${(t1 - t0).millis}")
//    println(s"avg time  : ${((t1 - t0).toDouble / total).round.millis}")
//  }
//  // every player plays every other, including themselves.
//  def tournament[G <: Game](newGame: G, matches: Int)(players: Seq[Player[G]]): Unit = {
//    players match {
//      case p1 :: p2 :: ps =>
//        head2head(newGame, matches, p1, p2)
//        tournament(newGame, matches)(p2 :: ps)
//      case _ => // done
//    }
//    players foreach { p1 =>
//      players foreach { p2 =>
//        head2head(newGame, matches, p1, p2)
//      }
//    }
//  }
//  // a list of players is challenged by a list of other players.
//  def comeAtMeBro[G <: Game](newGame: G, matches: Int)(players: Seq[Player[G]], challengers: Seq[Player[G]]): Unit = {
//    players foreach { player =>
//      challengers foreach { challenger =>
//        head2head(newGame, matches, player, challenger)
//      }
//    }
//  }
}
