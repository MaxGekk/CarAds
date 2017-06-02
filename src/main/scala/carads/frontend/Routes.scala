package carads.frontend

import carads.Logging
import org.json4s.{DefaultFormats, Formats}
import spray.http.HttpHeaders.RawHeader
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpService, Route}

trait Routes extends HttpService with Json4sJacksonSupport with Logging {
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
    }
  }
}

