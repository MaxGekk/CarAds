package carads.frontend

import carads.backend.{Record, Storage}
import org.json4s.{DefaultFormats, Formats}
import spray.routing.{HttpServiceActor, RequestContext}

import scala.util.{Failure, Success}

/** The actors receive CarAds API requests, handle them and send CarAds API responses or errors. */
class RequestHandler(settings: Settings) extends HttpServiceActor with Routes {
  import RequestHandler.logException

  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal
  override val receive = runRoute(routes)

  override def handleGet(ctx: RequestContext, getReq: GetReq): Unit = {
    settings.storage.get(getReq.id) match {
      case Success(rec) =>
        ctx.complete(GetResp(isSuccess = true, record = Some(Resp.convRec(rec)), error = None))
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
      case Success(_) =>
        ctx.complete(PutResp(isSuccess = true, error = None))
      case Failure(exception) =>
        logException(exception, putReq.jsonReq)
        ctx.complete(PutResp(isSuccess = false, error = Some(exception.getMessage)))
    }
  }

  override def handleDel(ctx: RequestContext, delReq: DelReq): Unit = {
    settings.storage.delete(delReq.id) match {
      case Success(_) =>
        ctx.complete(DelResp(isSuccess = true, error = None))
      case Failure(exception) =>
        logException(exception, delReq.jsonReq)
        ctx.complete(DelResp(isSuccess = false, error = Some(exception.getMessage)))
    }
  }

  override def handleGetAll(ctx: RequestContext, getAllReq: GetAllReq): Unit = {
    settings.storage.
        getAll(getAllReq.limit).
        map(Record.sort(_, getAllReq.sortby)).
        map(_.map(Resp.convRec)) match {
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
      case Success(_) =>
        ctx.complete(ModifyResp(isSuccess = true, error = None))
      case Failure(exception) =>
        logException(exception, modifyReq.jsonReq)
        ctx.complete(ModifyResp(isSuccess = false, error = Some(exception.getMessage)))
    }
  }
}

object RequestHandler extends carads.Logging {
  def logException(exception: Throwable, jsonReq: String) = {
    val stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(exception)
    log.warn(
      s"""Failed due to: ${exception.getMessage}:
         | === JSON Request ===
         | $jsonReq
         | === Stack trace ===
         | $stackTrace
         |""".stripMargin)
  }
}

case class Settings(storage: Storage)
