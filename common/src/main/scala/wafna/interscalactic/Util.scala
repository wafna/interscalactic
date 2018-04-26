package wafna.interscalactic
import java.io.Closeable
object Util {
  implicit class AutoCloseBracket[R <: Closeable](resource: R) {
    def autoClose[V](use: R => V): V =
      try use(resource)
      finally resource.close()
  }
  implicit class AutoCloseableBracket[R <: AutoCloseable](resource: R) {
    def autoClose[V](use: R => V): V =
      try use(resource)
      finally resource.close()
  }

  /**
    * Create, use, destroy pattern.
    */
  def bracket[T, R](create: => R)(dispose: R => Unit)(use: R => T): T = {
    val r = create
    try use(r)
    finally dispose(r)
  }
  def autoClose[R <: Closeable, T](r: R)(f: R => T): T =
    try f(r)
    finally r.close()
  def autoCloseable[R <: AutoCloseable, T](r: R)(f: R => T): T =
    try f(r)
    finally r.close()
  def wrapException[T](msg: => String)(errorProducingThingy: => T): T =
    try errorProducingThingy
    catch {
      case e: Throwable => throw new RuntimeException(msg, e)
    }
}
