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

class ModifyTests extends FreeSpec with Matchers with ScalaFutures with Json4sJacksonSupport {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val config = ConfigFactory.load()
  val storage = {
    val s = new MemStorage()
    s.put(Record(3, "Freelander", Diesel(), 50000, true, None, None))
    s
  }
  val settings = Settings(storage)
  val service = new Service(config, settings)

  class Fixture(val pipeline: HttpRequest => Future[ModifyResp]) {
    val reqTimeout = Timeout(2.seconds)
    def send(request: ModifyReq): Future[ModifyResp] =
      pipeline(Post(s"http://localhost:${service.actualPort.get}/modify", request))
  }

  def withFixture(testCode: Fixture => Any): Unit = {
    implicit val system = service.start()
    val pipeline: HttpRequest => Future[ModifyResp] = sendReceive ~> unmarshal[ModifyResp]
    try {
      testCode(new Fixture(pipeline))
    }
    finally {
      service.shutdown(system)
    }
  }

  "Modify the ad with" - {
    " new car" in withFixture { fixture => import fixture._
      val request = ModifyReq(id = 3, title = Some("Freelander 2"), None, None, None, None,None)

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        storage.get(3).get.title shouldBe "Freelander 2"
      }
    }
  }
}
