package carads

import carads.backend._
import carads.frontend._
import com.typesafe.config.ConfigFactory
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar._
import org.scalatest.{FreeSpec, Matchers}
import spray.client.pipelining._
import spray.http._
import spray.httpx.Json4sJacksonSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetTests extends FreeSpec with Matchers with ScalaFutures with Json4sJacksonSupport {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  val config = ConfigFactory.load()
  val storage = {
    val s = new MemStorage()
    s.put(Record(1, "Old Niva", Gasoline(), 8000, false, Some(80000), Some(Req.str2Date("1981-01-19"))))
    s.put(Record(2, "Audi", Gasoline(), 18000, false, Some(60000), Some(Req.str2Date("2009-01-19"))))
    s.put(Record(3, "Freelander", Diesel(), 50000, true, None, None))

    s
  }
  val settings = Settings(storage)
  val service = new Service(config, settings)

  class Fixture(val pipeline: HttpRequest => Future[GetResp]) {
    val reqTimeout = Timeout(2.seconds)
    def send(request: GetReq): Future[GetResp] =
      pipeline(Post(s"http://localhost:${service.actualPort.get}/get", request))
  }

  def withFixture(testCode: Fixture => Any): Unit = {
    implicit val system = service.start()
    val pipeline: HttpRequest => Future[GetResp] = sendReceive ~> unmarshal[GetResp]
    try {
      testCode(new Fixture(pipeline))
    }
    finally {
      service.shutdown(system)
    }
  }

  "Get an ad with" - {
    " new car" in withFixture { fixture => import fixture._
      val request = GetReq(3)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        resp.record shouldBe Some(
          RespRecord(3, "Freelander", "Diesel", 50000, true, None, None)        )
      }
    }
    " old car" in withFixture { fixture => import fixture._
      val request = GetReq(1)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        resp.record shouldBe Some(
          RespRecord(1, "Old Niva", "Gasoline", 8000, false, Some(80000), Some("1981-01-19"))
        )
      }
    }
  }
  "Get wrong ads" - {
    " - not existing" in withFixture { fixture => import fixture._
      val request = GetReq(100)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe false
      }
    }
  }
}



