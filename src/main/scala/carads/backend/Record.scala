package carads.backend

import java.util.Date

sealed trait Fuel {
  def compare(y: Fuel): Int
}
case class Gasoline() extends Fuel {
  override def toString: String = "Gasoline"
  def compare(y: Fuel): Int = y match {
    case _: Gasoline => 0
    case _ => 1
  }
}
case class Diesel() extends Fuel {
  override def toString: String = "Diesel"
  def compare(y: Fuel): Int = y match {
    case _: Diesel => 0
    case _ => -1
  }
}

case class Record(id: Int, title: String, fuel: Fuel, price: Int, `new`: Boolean,
                  mileage: Option[Int], registration: Option[Date])

object Record {
  def sort(records: List[Record], sortby: Option[String]): List[Record] = {
    implicit val ord = new Ordering[Record] {
      override def compare(x: Record, y: Record): Int = sortby match {
        case Some("title") => x.title.compare(y.title)
        case Some("price") => x.price.compare(y.price)
        case Some("new") => x.`new`.compare(y.`new`)
        case Some("fuel") => x.fuel.compare(y.fuel)
        case Some("mileage") =>
          implicitly[Ordering[Option[Int]]].compare(x.mileage, y.mileage)
        case Some("registration") =>
          implicitly[Ordering[Option[Date]]].compare(x.registration, y.registration)
        case _ => x.id.compare(y.id)
      }
    }
    records.sorted
  }
}