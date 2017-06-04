package carads

import java.text.SimpleDateFormat
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

class GetAllTests extends FreeSpec with Matchers with ScalaFutures with Json4sJacksonSupport {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val config = ConfigFactory.load()
  val storage = {
    val s = new MemStorage()
    s.put(Record(2, "Audi", Gasoline(), 35000, false, Some(60000), Some(format.parse("2009-01-19"))))
    s.put(Record(3, "Freelander", Diesel(), 30000, true, None, None))
    s.put(Record(1, "Old Niva", Gasoline(), 8000, false, Some(80000), Some(format.parse("1981-01-19"))))
    s
  }
  val settings = Settings(storage)
  val service = new Service(config, settings)

  class Fixture(val pipeline: HttpRequest => Future[GetAllResp]) {
    val reqTimeout = Timeout(2.seconds)
    def send(request: GetAllReq): Future[GetAllResp] =
      pipeline(Post(s"http://localhost:${service.actualPort.get}/all", request))
  }

  def withFixture(testCode: Fixture => Any): Unit = {
    implicit val system = service.start()
    val pipeline: HttpRequest => Future[GetAllResp] = sendReceive ~> unmarshal[GetAllResp]
    try {
      testCode(new Fixture(pipeline))
    }
    finally {
      service.shutdown(system)
    }
  }

  "Get all ads sorted by" - {
    " ids" in withFixture { fixture => import fixture._
      val request = GetAllReq(10, Some("id"))

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        resp.records shouldBe List(
          RespRecord(1, "Old Niva", "Gasoline", 8000, false, Some(80000), Some("1981-01-19")),
          RespRecord(2, "Audi", "Gasoline", 35000, false, Some(60000), Some("2009-01-19")),
          RespRecord(3, "Freelander", "Diesel", 30000, true, None, None)
        )
      }
    }
    " price" in withFixture { fixture => import fixture._
      val request = GetAllReq(10, Some("price"))

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        resp.records shouldBe List(
          RespRecord(1, "Old Niva", "Gasoline", 8000, false, Some(80000), Some("1981-01-19")),
          RespRecord(3, "Freelander", "Diesel", 30000, true, None, None),
          RespRecord(2, "Audi", "Gasoline", 35000, false, Some(60000), Some("2009-01-19"))
        )
      }
    }
    " registration" in withFixture { fixture => import fixture._
      val request = GetAllReq(10, Some("registration"))

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        resp.records shouldBe List(
          RespRecord(3, "Freelander", "Diesel", 30000, true, None, None),
          RespRecord(1, "Old Niva", "Gasoline", 8000, false, Some(80000), Some("1981-01-19")),
          RespRecord(2, "Audi", "Gasoline", 35000, false, Some(60000), Some("2009-01-19"))
        )
      }
    }
  }
}



