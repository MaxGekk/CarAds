package carads.frontend

import carads.backend.Storage
import org.json4s.{DefaultFormats, Formats}
import spray.routing.{HttpServiceActor, RequestContext}

import scala.util.{Failure, Success}

/** The actors receive Star API requests, handle them and send Star API responses or errors. */
class RequestHandler(settings: Settings) extends HttpServiceActor with Routes {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal
  override val receive = runRoute(routes)

  override def handlePut(ctx: RequestContext, putReq: PutReq): Unit = {
    val result = for {
      record <- putReq.record
      response <- settings.storage.put(record)
    } yield response

    result match {
      case Success(_) =>  ctx.complete(PutResp(isSuccess = true, error = None))
      case Failure(exception) =>
        logException(exception)
        ctx.complete(PutResp(isSuccess = false, error = Some(exception.getMessage)))
    }
  }

  def logException(exception: Throwable) = {
    val stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(exception)
    log.error(
      s"""Failed due to: ${exception.getMessage}:
         | === Stack trace ===
         | $stackTrace
         |""".stripMargin)
  }
}

case class Settings(storage: Storage)
