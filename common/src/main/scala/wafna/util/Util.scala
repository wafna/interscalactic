package wafna.interscalactic
import java.io.Closeable

import sun.awt.EventQueueItem

import scala.util.control.NoStackTrace
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
  object QuietException {
    def apply(msg: String): Nothing = throw new QuietException(msg)
  }
  class QuietException(msg: String) extends RuntimeException with NoStackTrace
}
