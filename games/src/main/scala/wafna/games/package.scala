package wafna
import wafna.games.Token.{P1, P2}
/**
  * General things for two player games.
  */
package object games {
  /**
    * Identifies the players.
    */
  sealed abstract class Token {
    def `unary_!`: Token = this match {
      case P1 => P2
      case P2 => P1
    }
  }
  object Token {
    case object P1 extends Token
    case object P2 extends Token
  }
  trait Player[G <: Game] {
    // used to control console output when playing interactively.
    val interactive: Boolean = false
    val name: String = getClass.getSimpleName
    def move(g: G): Option[(G, Any)]
  }
  /**
    * Defines a two player game.
    *
    * @param turnInHand The player with turn in hand for this game state. Not defined if the game is completed.
    * @param result Left(token or draw) or Right(available moves).
    * If the game continues (the Right option) the list must not be empty but, rather,
    * represent a pass.
    */
  abstract class Game(val turnInHand: Token, val result: Either[Option[Token], List[(Game, Any)]])
}