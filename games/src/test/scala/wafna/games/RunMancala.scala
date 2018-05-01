package wafna.games
/*
import java.util.Scanner
import wafna.games.Token.{P1, P2}
import scala.util.{Failure, Random, Success, Try}
/**
  * Scratch pad for bots and tweaks.
  */
object RunMancala extends App {
  class Human extends Player[Mancala] {
    override val interactive = true
    override def move(g: Mancala): Option[Mancala] = {
      println(g.showMancala)
      print(s"${g.currentPlayer}> ")
      new Scanner(System.in).nextLine
    } match {
      case "" =>
        val availableColumns = g.availableColumns
        if (availableColumns.isEmpty) Some(g.availableMoves.head)
        else move(g)
      case s => Try(s.toInt) match {
        case Failure(e) =>
          println(e)
          move(g)
        case Success(c) =>
          g.move(c) match {
            case Left(e) =>
              println(e)
              move(g)
            case Right(n) =>
              Some(n)
          }
      }
    }
  }
  class Random extends Player[Mancala] {
    override def move(g: Mancala): Option[Mancala] =
      Random.shuffle(g.availableMoves).headOption
  }
  def sortMoves2(p: Token, moves: List[Mancala]): List[Mancala] =
    moves.map(m => m -> (p match {
      case P1 => m.score._1 - m.score._2
      case P2 => m.score._2 - m.score._1
    })).sortBy(_._2)(Ordering[Int].reverse).map(_._1)
  class Shorty extends Player[Mancala] {
    override def move(g: Mancala): Option[Mancala] = sortMoves2(g.currentPlayer, g.availableMoves).headOption
  }
  class DeltaScore(maxDepth: Int) extends MinMax[Mancala, Int](maxDepth)(scala.math.Ordering.Int) {
    override val name = s"${getClass.getSimpleName}($maxDepth)"
    override def evaluate(player: Token, game: Mancala): Int = game.score match {
      case (s1, s2) => game.currentPlayer match {
        case P1 => s1 - s2
        case P2 => s2 - s1
      }
    }
  }
  class DeltaScoreR(maxDepth: Int) extends DeltaScore(maxDepth) {
    override def sortMoves(player: Token, moves: List[Mancala]): List[Mancala] =
      Random.shuffle(moves)
  }
  class DeltaScoreS(maxDepth: Int) extends DeltaScore(maxDepth) {
    override def sortMoves(player: Token, moves: List[Mancala]): List[Mancala] =
      sortMoves2(player, moves)
  }
  def fail(s: String): Nothing = {
    println(s)
    sys exit 1
  }
  def g(columns: Int, stones: Int) = Mancala(columns, stones) match {
    case Left(e) => fail(e)
    case Right(m) => m
  }
  def std = g(6, 4)
  // Game.comeAtMeBro[Mancala](g, 100)(List(new Random), (6 to 7).map(d => new DeltaScore(d)))
  def testMinMax(m: MinMax[Mancala, Int], matches: Int): Unit = {
    Game.head2head(std, matches, m, new Shorty)
    m.printStats()
  }
  // testMinMax(new DeltaScoreS(8), 10)
  val pA = new DeltaScoreS(4)
  val pB = new DeltaScoreR(6)
  Game.head2head(std, 1, pA, pB)
  pA.printStats()
  pB.printStats()
  // Game.comeAtMeBro(std, 20)(List(new Random, new Shorty), List(new DeltaScoreR(5), new DeltaScoreS(5)))
}*/
