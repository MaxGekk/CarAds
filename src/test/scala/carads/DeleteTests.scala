package carads

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

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

class DeleteTests extends FreeSpec with Matchers with ScalaFutures with Json4sJacksonSupport {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val config = ConfigFactory.load()
  val storage = {
    val s = new MemStorage()
    s.put(Record(1, "Old Niva", Gasoline(), 8000, false, Some(80000), Some(format.parse("1981-2-11"))))
    s.put(Record(2, "Audi", Gasoline(), 18000, false, Some(60000), Some(format.parse("2009-1-11"))))
    s.put(Record(3, "Freelander", Diesel(), 50000, true, None, None))

    s
  }
  val settings = Settings(storage)
  val service = new Service(config, settings)

  class Fixture(val pipeline: HttpRequest => Future[DelResp]) {
    val reqTimeout = Timeout(2.seconds)
    def send(request: DelReq): Future[DelResp] =
      pipeline(Post(s"http://localhost:${service.actualPort.get}/delete", request))
  }

  def withFixture(testCode: Fixture => Any): Unit = {
    implicit val system = service.start()
    val pipeline: HttpRequest => Future[DelResp] = sendReceive ~> unmarshal[DelResp]
    try {
      testCode(new Fixture(pipeline))
    }
    finally {
      service.shutdown(system)
    }
  }

  "Delete an ad with" - {
    " new car" in withFixture { fixture => import fixture._
      val request = DelReq(3)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
      }
    }
    " old car" in withFixture { fixture => import fixture._
      val request = DelReq(1)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
      }
    }
  }
}



