package carads.frontend

import java.text.SimpleDateFormat
import java.util.Date

import carads.backend.{Diesel, Gasoline, Record}

import scala.util.Try

case class Put(
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
