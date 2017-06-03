package carads.frontend

import carads.backend.Storage
import org.json4s.{DefaultFormats, Formats}
import spray.routing.{HttpServiceActor, RequestContext}
import scala.util.{Failure, Success}

/** The actors receive CarAds API requests, handle them and send CarAds API responses or errors. */
class RequestHandler(settings: Settings) extends HttpServiceActor with Routes {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal
  override val receive = runRoute(routes)

  override def handleGet(ctx: RequestContext, getReq: GetReq): Unit = {
    settings.storage.get(getReq.id) match {
      case Success(rec) =>
        ctx.complete(GetResp(isSuccess = true, record = Some(rec), error = None))
      case Failure(exception) =>
        logException(exception, getReq.jsonReq)
        ctx.complete(GetResp(isSuccess = false, error = Some(exception.getMessage), record = None))
    }
  }

  override def handlePut(ctx: RequestContext, putReq: PutReq): Unit = {
    val result = for {
      record <- putReq.record
      response <- settings.storage.put(record)
    } yield response

    result match {
      case Success(old) =>
        ctx.complete(PutResp(isSuccess = true, old = Some(old), error = None))
      case Failure(exception) =>
        logException(exception, putReq.jsonReq)
        ctx.complete(PutResp(isSuccess = false, error = Some(exception.getMessage), old = None))
    }
  }

  override def handleDel(ctx: RequestContext, delReq: DelReq): Unit = {
    settings.storage.delete(delReq.id) match {
      case Success(rec) =>
        ctx.complete(DelResp(isSuccess = true, old = Some(rec), error = None))
      case Failure(exception) =>
        logException(exception, delReq.jsonReq)
        ctx.complete(DelResp(isSuccess = false, error = Some(exception.getMessage), old = None))
    }
  }

  override def handleGetAll(ctx: RequestContext, getAllReq: GetAllReq): Unit = {
    settings.storage.getAll(getAllReq.limit) match {
      case Success(records) =>
        ctx.complete(GetAllResp(isSuccess = true, records = records, error = None))
      case Failure(exception) =>
        logException(exception, getAllReq.jsonReq)
        ctx.complete(GetAllResp(isSuccess = false, error = Some(exception.getMessage), records = List()))
    }
  }

  override def handleModify(ctx: RequestContext, modifyReq: ModifyReq): Unit = {
    val result = for {
      (record, attrs) <- modifyReq.record
      response <- settings.storage.modify(record, attrs)
    } yield response

    result match {
      case Success(old) =>
        ctx.complete(ModifyResp(isSuccess = true, old = Some(old), error = None))
      case Failure(exception) =>
        logException(exception, modifyReq.jsonReq)
        ctx.complete(ModifyResp(isSuccess = false, error = Some(exception.getMessage), old = None))
    }
  }

  def logException(exception: Throwable, jsonReq: String) = {
    val stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(exception)
    log.error(
      s"""Failed due to: ${exception.getMessage}:
         | === JSON Request ===
         | $jsonReq
         | === Stack trace ===
         | $stackTrace
         |""".stripMargin)
  }
}

case class Settings(storage: Storage)
