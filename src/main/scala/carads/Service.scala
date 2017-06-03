package carads

import akka.actor.{ActorSystem, OneForOneStrategy, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import spray.can.Http
import scala.concurrent.duration._
import concurrent.{Await, Future}
import akka.actor.SupervisorStrategy.Restart
import akka.routing.{DefaultResizer, SmallestMailboxPool}
import carads.backend.DynamoDb
import carads.frontend.{RequestHandler, Settings}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class Service(config: Config, settings: Settings) extends Logging {
  val WORKERS_AMOUNT = config.getInt("common.workers-amount")
  val BIND_TIMEOUT = 1 minute
  var actualPort: Option[Int] = None

  def start(): ActorSystem = {
    implicit val system = ActorSystem("CarAds")

    val handler = system.actorOf(
      props = SmallestMailboxPool(WORKERS_AMOUNT).
        withSupervisorStrategy(OneForOneStrategy(-1, Duration.Inf) { case _ => Restart }).
        withResizer(DefaultResizer(lowerBound = WORKERS_AMOUNT, upperBound = 2 * WORKERS_AMOUNT)).
        props(Props(classOf[RequestHandler], settings)),
      name = "request-handler"
    )

    implicit val askTimeout = Timeout.durationToTimeout(BIND_TIMEOUT)
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")

    val startedSystem = IO(Http).ask(Http.Bind(handler, interface, port)).flatMap {
      case boundInfo: Http.Bound =>
        actualPort = Some(boundInfo.localAddress.getPort)
        Future.successful(system)
      case _ => Future.failed(new RuntimeException("Binding failed."))
    }
    Await.result(startedSystem, BIND_TIMEOUT)
  }

  def shutdown(system: ActorSystem): Unit = {
    system.shutdown()
    system.awaitTermination(5.seconds)
  }
}

object Service {
  def configure(config: Config): Settings = {
    val storage = new DynamoDb {
      val tableName = config.getString("amazon.dynamodb.table")
      val credentials = new BasicAWSCredentials(
        config.getString("amazon.access-key"), config.getString("amazon.secret-key")
      );
      val client = AmazonDynamoDBClientBuilder.standard().
        withCredentials(new AWSStaticCredentialsProvider(credentials)).
        withEndpointConfiguration(new EndpointConfiguration(
          config.getString("amazon.dynamodb.url"),
          config.getString("amazon.dynamodb.region")
        )).
        build()
    }
    Settings(storage)
  }

  def main(args: Array[String]): Unit = try {
    val config = ConfigFactory.load
    val service = new Service(config, configure(config))
    val system = service.start()
    sys.addShutdownHook(service.shutdown(system))
  } catch {
    case e: Throwable =>
      val log = LoggerFactory.getLogger(this.getClass.getName)
      val stackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e)
      log.error(s"CarAds crashed due to $stackTrace")
      throw e
  }
}

