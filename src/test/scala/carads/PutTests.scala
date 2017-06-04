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
import scala.util.Success

class PutTests extends FreeSpec with Matchers with ScalaFutures with Json4sJacksonSupport {
  override implicit def json4sJacksonFormats: Formats = DefaultFormats.withBigDecimal

  val format = new SimpleDateFormat("yyyy-MM-dd")
  val config = ConfigFactory.load()
  val storage = new MemStorage()
  val settings = Settings(storage)
  val service = new Service(config, settings)

  class Fixture(val pipeline: HttpRequest => Future[PutResp]) {
    val reqTimeout = Timeout(2.seconds)
    def send(request: PutReq): Future[PutResp] =
      pipeline(Post(s"http://localhost:${service.actualPort.get}/put", request))
  }

  def withFixture(testCode: Fixture => Any): Unit = {
    implicit val system = service.start()
    val pipeline: HttpRequest => Future[PutResp] = sendReceive ~> unmarshal[PutResp]
    try {
      testCode(new Fixture(pipeline))
    }
    finally {
      service.shutdown(system)
    }
  }

  "Put an ad with" - {
    " new car" in withFixture { fixture => import fixture._
      val request = PutReq(
        id = 1,
        title = "Honda",
        fuel = "Gasoline",
        price = 25000,
        `new` = true,
        mileage = None, registration = None
      )

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        storage.get(1) shouldBe Success(
          Record(1, "Honda", Gasoline(), 25000, true, None, None)
        )
      }
    }
    " old car" in withFixture { fixture => import fixture._
      val request = PutReq(
        id = 2,
        title = "Audi",
        fuel = "Diesel",
        price = 20000,
        `new` = false,
        mileage = Some(120000), registration = Some("2009-10-06")
      )

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe true
        resp.error shouldBe None
        storage.get(2) shouldBe Success(
          Record(2, "Audi", Diesel(), 20000, false, Some(120000), Some(format.parse("2009-10-6")))
        )
      }
    }
  }
  "Put invalid ads with" - {
    " wrong fuel" in withFixture { fixture => import fixture._
      val request = PutReq(
        id = 3,
        title = "Honda",
        fuel = "Water",
        price = Int.MaxValue,
        `new` = true,
        mileage = None, registration = None
      )

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe false
        resp.error shouldBe Some("unknown fuel: Water")
      }
    }
    " old car but unknown mileage" in withFixture { fixture => import fixture._
      val request = PutReq(
        id = 4,
        title = "BMW",
        fuel = "Diesel",
        price = 50000,
        `new` = false,
        mileage = None, registration = None
      )

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe false
        resp.error shouldBe Some("Old car must have the mileage and first registration")
      }
    }
    " new car with mileage" in withFixture { fixture => import fixture._
      val request = PutReq(
        id = 5,
        title = "BMW",
        fuel = "Diesel",
        price = 50000,
        `new` = true,
        mileage = Some(120000), registration = None
      )

      whenReady(send(request), reqTimeout) { resp =>
        resp.isSuccess shouldBe false
        resp.error shouldBe Some("New car shouldn't have the mileage and first registration")
      }
    }
  }
}



