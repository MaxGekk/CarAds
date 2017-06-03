package carads.frontend

import java.text.SimpleDateFormat
import carads.backend.{Diesel, Gasoline, Record}
import scala.util.Try

case class PutReq(
                id: Int,
                title: String,
                fuel: String,
                price: Int,
                `new`: Boolean,
                mileage: Option[Int],
                registration: Option[String]
              ) {
  var jsonReq: String = "Unknown"
  def record: Try[Record] = {
    val result = for {
      parsedFuel <- Try { fuel match {
        case "Gasoline" => Gasoline()
        case "Diesel" => Diesel()
        case unknown => throw new IllegalArgumentException(s"unknown fuel: $unknown")
      }}
      parsedReg = registration.map(date =>
        Try { new SimpleDateFormat("yyyy.MM.dd").parse(date)}.toOption
      ).flatten
    } yield Record(id, title, parsedFuel, price, `new`, mileage, parsedReg)

    result
  }
}

case class PutResp(
                    isSuccess: Boolean,
                    error: Option[String],
                    old: Option[Record]
                  )

case class GetReq(id: Int) { var jsonReq: String = "Unknown" }
case class GetResp(
                  isSuccess: Boolean,
                  error: Option[String],
                  record: Option[Record]
                  )

case class DelReq(id: Int) { var jsonReq: String = "Unknown" }
case class DelResp(
                    isSuccess: Boolean,
                    error: Option[String],
                    old: Option[Record]
                  )

case class GetAllReq(limit: Int) { var jsonReq: String = "Unknown" }
case class GetAllResp(
                    isSuccess: Boolean,
                    error: Option[String],
                    records: List[Record]
                  )
