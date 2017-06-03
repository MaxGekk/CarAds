package carads.frontend

import carads.Logging
import org.json4s.{DefaultFormats, Formats}
import spray.http.HttpHeaders.RawHeader
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpService, RequestContext, Route}

trait Routes extends HttpService with Json4sJacksonSupport with Logging {
  def handlePut(ctx: RequestContext, request: PutReq): Unit
  def handleGet(ctx: RequestContext, putReq: GetReq): Unit
  def handleDel(ctx: RequestContext, delReq: DelReq): Unit

  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  def rawJson = extract { _.request.entity.asString}

  // X-Powered-By returns a info about our app in every response
  def respondWithXPowerByHeader = respondWithHeaders {
    RawHeader("X-Powered-By", "CarAds")
  }

  val routes = Route {
    path("info") {
      respondWithXPowerByHeader {
        complete(s"Car Ads v1.0")
      }
    } ~ path("status") {
      respondWithXPowerByHeader {
        complete("OK")
      }
    } ~ path("put") {
      rawJson { jsonReq =>
        post {
          respondWithXPowerByHeader {
            entity(as[PutReq]) { putReq =>
              log.info(s"Serving put path: $jsonReq")
              putReq.jsonReq = jsonReq
              ctx => handlePut(ctx, putReq)
            }
          }
        }
      }
    }
  } ~ path("get") {
    rawJson { jsonReq =>
      post {
        respondWithXPowerByHeader {
          entity(as[GetReq]) { getReq =>
            log.info(s"Serving get path: $jsonReq")
            getReq.jsonReq = jsonReq
            ctx => handleGet(ctx, getReq)
          }
        }
      }
    }
  } ~ path("delete") {
    rawJson { jsonReq =>
      post {
        respondWithXPowerByHeader {
          entity(as[DelReq]) { delReq =>
            log.info(s"Serving get path: $jsonReq")
            delReq.jsonReq = jsonReq
            ctx => handleDel(ctx, delReq)
          }
        }
      }
    }
  }
}

