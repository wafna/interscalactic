package wafna.games
import wafna.games.Hexapawn.{Board, Direction}
import wafna.games.Token.{P1, P2}
object Hexapawn {
  /**
    * The ways a piece can move.
    */
  object Direction extends Enumeration {
    type Move = Value
    val Forward, CaptureLeft, CaptureRight = Value
  }
  final case class Move(from: (Int, Int), direction: Direction.Value)
  object Board {
    // Useful to avoid logic error when iterating over columns and rows.
    val indexer: Range = 0 until 3
  }
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
    @inline def toIndex(p: (Int, Int)): Int = toIndex(p._1, p._2)
    @inline def at(x: Int, y: Int): Option[Token] = array(toIndex(x, y))
    @inline def at(p: (Int, Int)): Option[Token] = at(p._1, p._2)
    @inline def inBounds(p: (Int, Int)): Boolean = (p._1 >= 0) && (p._1 < 3) && (p._2 >= 0) && (p._2 < 3)
    /**
      * All the ways to move.  This only produces legal boards.
      *
      * @param from Must be occupied, direction is inferred from the token.
      * @param direction The direction of the move.
      * @return
      */
    def move(from: (Int, Int), direction: Direction.Value): Either[String, Board] = {
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
              val dest = (from._1, from._2 + delta)
              if (at(dest).isEmpty) Right(dest)
              else Left(s"Can only move forward to unoccupied space.")
            case Direction.CaptureLeft =>
              val dest = (from._1 - delta, from._2 + delta)
              if (at(dest).contains(movingPlayer.opponent)) Right(dest)
              else Left(s"Can only capture an opponent.")
            case Direction.CaptureRight =>
              val dest = (from._1 + delta, from._2 + delta)
              if (at(dest).contains(movingPlayer.opponent)) Right(dest)
              else Left(s"Can only capture an opponent.")
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
    import Board.indexer
    /**
      * Evaluate the game relative to the player.
      */
    override def evaluate(player: Token, game: Hexapawn): Int = {
      val (homeRow, goalRow) = player match {
        case P1 => (0, 2)
        case P2 => (2, 0)
      }
      if (indexer.exists(x => game.board.at(x, goalRow).contains(player)))
        1000 // player win
      else if (indexer.exists(x => game.board.at(x, homeRow).contains(player.opponent)))
        -1000 // player loss
      else {
        // no decision, but we could explore nuances in intermediate game states
        0
      }
    }
  }
}
/**
  * Even simpler than tic-tac-toe.  Search for Martin Gardener's excellent treatment of the game in Scientific American.
  * It has a nice property that it has few initial symmetries (presumably no more than one) so the game tree has
  * low redundancy (and is quite small, anyway).
  *
  * @param player Turn in hand.
  * @param board Current board configuration.
  */
class Hexapawn private[games](val player: Token, val board: Board) extends Game {
  def this(player: Token) = this(player, new Board())
  /**
    * The player with turn in hand.
    */
  override def turnInHand: Token = player
  /**
    * Left(token or draw) or Right(available moves).
    */
  override def result: Either[Option[Token], List[(Hexapawn, Any)]] = {
    val opponent = player.opponent
    def checkWin(player: Token): Boolean = {
      val goalLine = player match {
        case P1 => 2
        case P2 => 0
      }
      Board.indexer.map(x => board.at(x, goalLine)).exists(_.contains(player))
    }
    // Check the opponent first in case the last moving player has just won the game.
    if (checkWin(opponent))
      Left(Some(opponent))
    else if (checkWin(player))
      Left(Some(player))
    else {
      // Nobody has won the game; find all the legal moves.
      import Board.indexer
      Right((for (x <- indexer; y <- indexer) yield (x, y)).filter(p => (board at p) contains player).toList flatMap { p =>
        Direction.values.flatMap(direction => board.move(p, direction).toOption).map(b => (new Hexapawn(opponent, b), p))
      })
    }
  }
}
