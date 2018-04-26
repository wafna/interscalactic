package wafna.interscalactic.server

import java.io._

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.scaladsl.FileIO
import akka.util.Timeout
import org.apache.tika.Tika

import scala.concurrent.duration._
class StaticContent
object StaticContent {
  // we leave these abstract, since they will be provided by the App
  private implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  private final case class FileDesc(mimeType: String, binary: Boolean, compressible: Boolean)
  private val fileMap: Map[String, FileDesc] = Map(
    "html" -> FileDesc("text/html", binary = false, compressible = true),
    "css" -> FileDesc("text/css", binary = false, compressible = true),
    "js" -> FileDesc("application/x-javascript", binary = false, compressible = true),
    "map" -> FileDesc("application/json", binary = false, compressible = true),
    "ico" -> FileDesc("image/x-icon", binary = true, compressible = false),
    "jpg" -> FileDesc("image/jpg", binary = true, compressible = false),
    "gif" -> FileDesc("image/gif", binary = true, compressible = false),
    "png" -> FileDesc("image/png", binary = true, compressible = false),
    "woff" -> FileDesc("application/font-woff", binary = true, compressible = false),
    "woff2" -> FileDesc("application/font-woff2", binary = true, compressible = false),
    "eot" -> FileDesc("application/vnd.ms-fontobject", binary = true, compressible = false),
    "svg" -> FileDesc("image/svg+xml", binary = true, compressible = true),
    "ttf" -> FileDesc("application/x-font-ttf", binary = true, compressible = false),
    "otf" -> FileDesc("font/opentype", binary = true, compressible = false)
  )
  private val tika = new Tika()
  def route(system: ActorSystem, webDir: File): Route = {
    lazy val log = Logging(system, classOf[StaticContent])

    /**
      * Examines the file name and returns a type description.
      */
    def describeFileType(fileName: String): FileDesc = {
      val ext = {
        var n = fileName
        // chop off leading directory bits just in case they have a dot in them
        val pSlash = n.lastIndexOf('/')
        if (pSlash >= 0) n = n.substring(pSlash)
        val pDot = n.lastIndexOf('.')
        if (pDot < 0) "" else n.substring(1 + pDot) // could fail if dot is at end?
      }
      fileMap.get(ext.toLowerCase) match {
        case Some(fd) => fd
        case None =>
          log.warning(s"Unknown file extension: $fileName")
          FileDesc("application/octet-stream", binary = true, compressible = true)
      }
    }
    path(Remaining) { path =>
      val target = new File(webDir, if (path.isEmpty) "index.html" else path)
      complete {
        try {
          ContentType.parse(tika detect target) match {
            case Right(contentType) =>
              HttpResponse(entity = HttpEntity(contentType, FileIO.fromPath(target.toPath)))
            case Left(errors) =>
              HttpResponse(StatusCodes.InternalServerError, entity = s"Can't determine MIME type: ${errors.mkString(" ")}")
          }
        } catch {
          case e: Throwable =>
            log.error( /*e,*/ s"static request failed: requested=$path, target=${target.getAbsolutePath}")
            HttpResponse(StatusCodes.NotFound, Nil, path)
        }
      }
    }
  }
}
