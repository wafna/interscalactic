package wafna.games
/*
import wafna.games.Mancala._
import wafna.games.Token.{P1, P2}
object Mancala {
  implicit class MancalaCLIPrinter(val m: Mancala) {
    // shows the board from the current player's perspective with the currentPlayer
    // on the bottom.
    def showMancala: String = showMancala()
    def showMancala(indent: String = ""): String = {
      val b = m.currentPlayer match {
        case P1 =>
          s"""${indent}P2 ${m.board.map(_.p2).map(v => f"$v%3d") mkString " "}
             |${indent}P1 ${m.board.map(_.p1).map(v => f"$v%3d") mkString " "}""".stripMargin
        case P2 =>
          s"""${indent}P1 ${m.board.reverse.map(_.p1).map(v => f"$v%3d") mkString " "}
             |${indent}P2 ${m.board.reverse.map(_.p2).map(v => f"$v%3d") mkString " "}""".stripMargin
      }
      // ${indent}moves: [${m.moves.length}] = ${m.moves.map(_.getOrElse("-")).mkString(", ")}
      s"""${indent}score: ${m.score}
         |${indent}moves: [${m.moves.length}] = ${m.moves.map(_.getOrElse("-")).mkString(", ")}
         |$b
         |$indent    ${m.board.indices.map(n => f"[$n]") mkString " "}""".stripMargin
    }
  }
  type M[T] = Either[String, T]
  type Score = (Int, Int)
  type Board = Array[Column]
  def apply(columns: Int, stones: Int): M[Mancala] = {
    if (1 > columns)
      Left(s"Columns must be positive: $columns")
    else if (1 > stones)
      Left(s"Stones must be positive: $stones")
    else
      Right(new Mancala(Token.P1, 0 -> 0, Array.fill(columns)(Column(stones, stones)), Nil))
  }
  // A column of the board is the span of two wells, on either player's side.
  // By grouping this way we ensure we have a consistent board.
  case class Column(p1: Int, p2: Int) {
    def stones(player: Token): Int = player match {
      case Token.P1 => p1
      case Token.P2 => p2
    }
  }
}
/**
  * do the right thing and put something here.
  */
class Mancala private(cp: Token, val score: Score, val board: Board, val moves: List[Option[Int]]) extends Game {
  private def translateColumn(n: Int): Int = {
    currentPlayer match {
      case P1 => n
      case P2 => board.length - n - 1
    }
  }
  def column(n: Int): Column = board(translateColumn(n))
  /**
    * The player with turn in hand.
    */
  override def currentPlayer: Token = cp
  /**
    * Game over.
    */
  override def gameOver: Option[Option[Token]] =
    if (board.forall(c => 0 == c.p1 && 0 == c.p2)) {
      Some(if (score._1 > score._2) Some(P1)
      else if (score._2 > score._1) Some(P2)
      else None)
    } else None
  /**
    * The moves available to the current player from this game state.
    */
  override def availableMoves: List[Mancala] = {
    availableColumns match {
      case Nil =>
        // There are no cells with stones in the cp's row.
        // In a forced pass, your only "move" is to simply cede to the other player.
        List(new Mancala(cp.opp, score, board, moves))
      case mvs =>
        mvs map moveColumn
    }
  }
  def availableColumns: List[Int] =
    board.indices.filter(i => 0 < board(i).stones(currentPlayer)).toList.map(translateColumn)
  // throws for illegal moves.
  private def moveColumn(col: Int): Mancala = {
    def mapBoard(board: Board, col: Int)(f: Column => Column): Board =
      board.zipWithIndex map {
        case (c, i) => if (i == col) f(c) else c
      }
    if (0 > col || board.length <= col)
      sys error s"Invalid move for $currentPlayer: column out of bounds: $col ${board.length}.\n${this.showMancala}"
    val stonesInHand = board(translateColumn(col)).stones(currentPlayer)
    if (1 > stonesInHand)
      sys error s"Invalid move for $currentPlayer: column $col is empty.\n${this.showMancala}."
    // The side of the board we're 'on' (i.e. the side to which the virtual column points).
    var currentSide = currentPlayer
    object columnTranslator {
      private var _virtualColumn: Int = col
      private var _actualColumn = __actualColumn
      def virtualColumn: Int = _virtualColumn
      def actualColumn: Int = _actualColumn
      // Translate the virtual column to the actual column for indexing the board.
      private def __actualColumn: Int = (currentPlayer, currentSide) match {
        case (P1, P1) => _virtualColumn
        case (P1, P2) => board.length - _virtualColumn - 1
        case (P2, P1) => _virtualColumn
        case (P2, P2) => board.length - _virtualColumn - 1
      }
      def bump(): Unit = {
        _virtualColumn += 1
        try {
          if (_virtualColumn == board.length) {
            _virtualColumn = 0
            currentSide = currentSide.opp
          }
        } finally {
          _actualColumn = __actualColumn
        }
        // println(s"bump: v = ${_virtualColumn}, a = ${_actualColumn}, $currentPlayer, $currentSide")
      }
    }
    val pickedUp = new Mancala(currentPlayer, score, mapBoard(board, columnTranslator.actualColumn)(
      c => currentPlayer match {
        case P1 => Column(0, c.p2)
        case P2 => Column(c.p1, 0)
      }), moves)
    val afterMoves: Mancala = {
      // On each side of the board, the columns go up from zero.
      // We account for this perspective at the point where we actually update the board.
      // All this seems to make the logic simpler than having lots of crazy modular arithmetic.
      var inHand = stonesInHand
      // the game state after placing each stone
      var currentState = pickedUp
      // we test the actual currentPlayer here because scores are never made for the opponent
      // during a move.
      // When the move ends on own goal.
      var swish = false
      while (0 < inHand) {
        columnTranslator.bump()
        if (columnTranslator.virtualColumn == 0) {
          if (currentSide != currentPlayer) {
            val newScore = currentPlayer match {
              case P1 => (currentState.score._1 + 1, currentState.score._2)
              case P2 => (currentState.score._1, currentState.score._2 + 1)
            }
            currentState = new Mancala(currentPlayer, newScore, currentState.board, currentState.moves)
            inHand -= 1 // deduct one for our goal
            if (0 == inHand) swish = true
          }
        }
        // we may have deducted one for a score so we have to check, again.
        if (0 < inHand) {
          currentState = new Mancala(currentPlayer, currentState.score,
            mapBoard(currentState.board, columnTranslator.actualColumn) {
              case Column(s1, s2) => (currentPlayer, currentSide) match {
                case (P1, P1) => Column(s1 + 1, s2)
                case (P1, P2) => Column(s1, s2 + 1)
                case (P2, P1) => Column(s1 + 1, s2)
                case (P2, P2) => Column(s1, s2 + 1)
              }
            }, currentState.moves)
          inHand -= 1
        }
      }
      // Having placed all the stones, check the wacky end of turn conditions.
      currentState = if (0 == columnTranslator.virtualColumn && swish && currentPlayer != currentSide) {
        // We has ended on own goal and, thus, keep turn in hand.
        currentState
      } else {
        val actual = currentState.board(columnTranslator.actualColumn)
        // If we dropped the last stone in an empty well on our own side,
        // we remove all the stones on the other side, credit them to our
        // score, and switch players.
        if (currentPlayer == currentSide && 1 == (currentPlayer match {
          case P1 => actual.p1
          case P2 => actual.p2
        })) {
          val scoreWithSteal = currentPlayer match {
            case P1 => (currentState.score._1 + actual.p2) -> currentState.score._2
            case P2 => currentState.score._1 -> (currentState.score._2 + actual.p1)
          }
          val mapBoardWithSteal = mapBoard(currentState.board, columnTranslator.actualColumn)(c => currentPlayer match {
            case P1 => Column(c.p1, 0)
            case P2 => Column(0, c.p2)
          })
          new Mancala(currentPlayer.opp, scoreWithSteal, mapBoardWithSteal, moves)
        } else {
          // Switch players.
          new Mancala(currentPlayer.opp, currentState.score, currentState.board, moves)
        }
      }
      currentState
    }
    // List thing to do is stick the current move at the end.
    new Mancala(afterMoves.currentPlayer, afterMoves.score, afterMoves.board, Some(col) :: afterMoves.moves)
  }
  //
  // returns a new game state if the move is legal.
  /**
    * This is provided to interpret moves specified as columns on the board.
    * Useful for human interaction.
    * The column is interpreted in the context of the current player, i.e. the column
    * is relative to the moving player, numbered left to right.
    */
  def move(col: Int): M[Mancala] = {
    if (0 > col || board.length <= col)
      Left(s"Invalid move for $currentPlayer: column out of bounds: $col ${board.length}.\n${this.showMancala}")
    else {
      val stonesInHand = column(col).stones(currentPlayer)
      if (1 > stonesInHand) {
        Left(s"Invalid move for $currentPlayer: column $col is empty.\n${this.showMancala}.")
      } else {
        Right(moveColumn(col))
      }
    }
  }
}*/
