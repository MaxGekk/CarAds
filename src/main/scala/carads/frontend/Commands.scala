package carads.frontend

import java.text.SimpleDateFormat
import java.util.Date
import carads.backend.{Diesel, Gasoline, Record}
import scala.collection.mutable
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
      parsedReg <- Try { registration match {
        case Some(str) => Some(Req.str2Date(str))
        case None => None
      }}
    } yield Record(id, title, parsedFuel, price, `new`, mileage, parsedReg)

    result
  }
}

case class RespRecord(
                       id: Int,
                       title: String,
                       fuel: String,
                       price: Int,
                       `new`: Boolean,
                       mileage: Option[Int],
                       registration: Option[String]
                     )
case class PutResp(
                    isSuccess: Boolean,
                    error: Option[String]
                  )

case class GetReq(id: Int) { var jsonReq: String = "Unknown" }
case class GetResp(
                  isSuccess: Boolean,
                  error: Option[String],
                  record: Option[RespRecord]
                  )

case class DelReq(id: Int) { var jsonReq: String = "Unknown" }
case class DelResp(
                    isSuccess: Boolean,
                    error: Option[String]
                  )

case class GetAllReq(limit: Int) { var jsonReq: String = "Unknown" }
case class GetAllResp(
                    isSuccess: Boolean,
                    error: Option[String],
                    records: List[RespRecord]
                  )

case class ModifyReq(
                   id: Int,
                   title: Option[String],
                   fuel: Option[String],
                   price: Option[Int],
                   `new`: Option[Boolean],
                   mileage: Option[Int],
                   registration: Option[String]
                 ) {
  var jsonReq: String = "Unknown"
  def record: Try[(Record, Set[String])] = {
    Try {
      var rec = Record(id, "", Gasoline(), 0, true, None, None)
      var attrs = mutable.Set[String]()
      if (title.isDefined) {
         attrs += "title"
         rec = rec.copy(title = title.get)
      }
      if (fuel.isDefined) {
        attrs += "fuel"
        fuel.get match {
          case "Gasoline" => rec = rec.copy(fuel = Gasoline())
          case "Diesel" => rec = rec.copy(fuel = Diesel())
        }
      }
      if (price.isDefined) {
        attrs += "price"
        rec = rec.copy(price = price.get)
      }
      if (`new`.isDefined) {
        attrs += "new"
        rec = rec.copy(`new` = `new`.get)
      }
      if (mileage.isDefined) {
        attrs += "mileage"
        rec = rec.copy(mileage = mileage)
      }
      if (registration.isDefined) {
        attrs += "registration"
        rec = rec.copy(registration = Some(Req.str2Date(registration.get)))
      }
      (rec, attrs.toSet)
    }
  }
}
case class ModifyResp(
                    isSuccess: Boolean,
                    error: Option[String]
                  )

object Req {
  val pattern = new SimpleDateFormat("yyyy-MM-dd")
  def str2Date(str: String): Date = {
    pattern.parse(str)
  }
}

object Resp {
  def date2Str(date: Date): String = {
    Req.pattern.format(date)
  }
  def convRec(record: Record): RespRecord = {
    RespRecord(
      record.id, record.title, record.fuel.toString, record.price,
      record.`new`, record.mileage, record.registration.map(date2Str)
    )
  }
}
