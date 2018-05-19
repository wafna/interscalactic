package wafna.games
import org.scalatest.FlatSpec
import wafna.games.Token.{P1, P2}
class TestMinMax extends FlatSpec{
  import MinMax._
  "MinMax" should "compare evaluations" in {
    assert(0 == compareEvaluations(P1, Left(None), Left(None)))
    assert(0 == compareEvaluations(P1, Left(Some(P1)), Left(Some(P1))))
    assert(0 == compareEvaluations(P1, Left(Some(P2)), Left(Some(P2))))
  }
}
