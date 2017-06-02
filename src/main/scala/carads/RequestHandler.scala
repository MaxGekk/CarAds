package carads

import org.json4s.{DefaultFormats, Formats, _}
import spray.routing.{HttpServiceActor, _}

/** The actors receive Star API requests, handle them and send Star API responses or errors. */
class RequestHandler(settings: Settings) extends HttpServiceActor with Routes {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  override val receive = runRoute(routes)

  def logException(exception: Throwable) = {
    val stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(exception)
    log.error(
      s"""Failed due to: ${exception.getMessage}:
         | === Stack trace ===
         | $stackTrace
         |""".stripMargin)
  }
}

case class Settings()
