package wafna.games
import wafna.games.Hexapawn.Board
import wafna.games.MinMax.Eval
import wafna.games.Token.{P1, P2}
/**
  * Even simpler than tic-tac-toe.  Search for Martin Gardener's excellent treatment of the game in Scientific American.
  * It has a nice property that it has few symmetries (presumably no more than one) so the game tree has
  * low redundancy (and is quite small, anyway).
  *
  * @param player Turn in hand.
  * @param board Current board configuration.
  */
class Hexapawn private[games](player: Token, val board: Board, val moves: List[Hexapawn.Move]) extends Game(player, Hexapawn.result(player, board, moves)) {
  def this(player: Token) = this(player, new Board(), Nil)
}
object Hexapawn {
  final case class Point(x: Int, y: Int)
  sealed abstract class Turn
  final case class Move(player: Token, from: Point, direction: Direction) extends Turn
  final case class Pass(player: Token) extends Turn
  type Result = Either[Option[Token], List[(Hexapawn, Any)]]
  /**
    * The ways a piece can move.
    */
  sealed abstract class Direction
  object Direction {
    case object Forward extends Direction
    case object CaptureLeft extends Direction
    case object CaptureRight extends Direction
    val values = List(Forward, CaptureLeft, CaptureRight)
  }
  /**
    * The extend of a single dimension (the board is square).
    * Useful to avoid logic error when iterating over columns and rows.
    */
  val indices: Range = 0 until 3
  /**
    * Encodes a board and all the legal transformations of one board to another.
    *
    * @param array The configuration of the board.
    */
  class Board private[games](array: Array[Option[Token]]) {
    def this() = this(Array(
      Some(P1), Some(P1), Some(P1),
      None, None, None,
      Some(P2), Some(P2), Some(P2))
    )
    @inline def toIndex(x: Int, y: Int): Int = x + 3 * y
    @inline def toIndex(p: Point): Int = toIndex(p.x, p.y)
    @inline def at(x: Int, y: Int): Option[Token] = array(toIndex(x, y))
    @inline def at(p: Point): Option[Token] = at(p.x, p.y)
    @inline def inBounds(p: Point): Boolean = (p.x >= 0) && (p.x < 3) && (p.y >= 0) && (p.y < 3)
    /**
      * All the ways to move.  This only produces legal boards.
      *
      * @param from Must be occupied, direction is inferred from the token.
      * @param direction The direction of the move.
      * @return
      */
    def move(from: Point, direction: Direction): Either[String, Board] = {
      def checkPoint(x: Int, y: Int, predicate: Point => Boolean, msg: => String): Either[String, Point] =
        if (x < 3 && x >= 0 && y < 3 && y >= 0) {
          val point = Point(x, y)
          if (predicate(point)) {
            Right(point)
          } else Left(msg)
        } else Left("Out of bounds.")
      at(from) match {
        case None =>
          Left(s"No player at position $from")
        case Some(movingPlayer) =>
          // used for both the y axis change and the absolute direction for captures left and right.
          val delta = movingPlayer match {
            case P1 => 1
            case P2 => -1
          }
          val to = direction match {
            case Direction.Forward =>
              checkPoint(from.x, from.y + delta, at(_).isEmpty, s"Can only move forward to unoccupied space.")
            case Direction.CaptureLeft =>
              checkPoint(from.x - delta, from.y + delta, at(_).contains(!movingPlayer), s"Can only capture an opponent.")
            case Direction.CaptureRight =>
              checkPoint(from.x + delta, from.y + delta, at(_).contains(!movingPlayer), s"Can only capture an opponent.")
          }
          to match {
            case Right(p) =>
              if (!inBounds(p))
                Left(s"Illegal move ($direction): off board: $p")
              else {
                val newArray = array.clone()
                newArray(toIndex(from)) = None
                newArray(toIndex(p)) = Some(movingPlayer)
                Right(new Board(newArray))
              }
            case Left(e) => Left(e)
          }
      }
    }
  }
  object MinMaxHP extends MinMax[Hexapawn] {
    /**
      * Evaluate the game relative to the player.
      */
    override def evaluate(player: Token, game: Hexapawn): Eval = {
      game.result match {
        case Left(r) => Left(r)
        case Right(ms) => Right(0)
      }
    }
  }
  /**
    * Left(token or draw) or Right(available moves).
    */
  def result(player: Token, board: Hexapawn.Board, moves: List[Hexapawn.Move]): Either[Option[Token], List[(Hexapawn, Hexapawn.Move)]] = {
    val opponent = !player
    def checkWin(player: Token): Boolean = {
      val goalLine = player match {
        case P1 => 2
        case P2 => 0
      }
      indices.map(x => board.at(x, goalLine)).exists(_.contains(player))
    }
    // Check the opponent first in case the last moving player has just won the game.
    if (checkWin(opponent))
      Left(Some(opponent))
    else if (checkWin(player))
      Left(Some(player))
    else {
      // Nobody has won the game; find all the legal moves.
      Right((for (x <- indices; y <- indices) yield Point(x, y)).filter(p => board.at(p) contains player).toList flatMap { p =>
        Direction.values.flatMap(direction => board.move(p, direction).toOption.map(direction -> _)).map({
          case (d, b) => (new Hexapawn(opponent, b, Move(player, p, d) :: moves), p)
        })
      })
    }
  }
}
