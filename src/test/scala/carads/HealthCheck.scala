package carads

import akka.actor.ActorSystem
import carads.frontend.Settings
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar._
import org.scalatest.{FreeSpec, Matchers}
import spray.client.pipelining._
import spray.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class HealthCheck extends FreeSpec with Matchers with ScalaFutures {
  val config = ConfigFactory.load()
  val settings = Settings(storage = new MemStorage())
  val service = new Service(config, settings)

  "Service should" - {
    "return status in 300 milliseconds" in {
      implicit val tester = ActorSystem()

      val carAdsActors = service.start()
      def get(timeout: Span) = {
        implicit val reqTimeout = Timeout(timeout)
        val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
        val response = pipeline(Get(s"http://localhost:${service.actualPort.get}/status"))

        Await.result(response, timeout).status.isSuccess shouldBe true
      }
      try {
        get(1000.milliseconds)
        for (i <- 0 until 2) {
          get(300.milliseconds)
        }
      } finally {
        service.shutdown(carAdsActors)
      }
    }
  }
}



