package carads.backend

import java.util.Date

sealed trait Fuel
case class Gasoline() extends Fuel {
  override def toString: String = "Gasoline"
}
case class Diesel() extends Fuel {
  override def toString: String = "Diesel"
}

case class Record(id: Int, title: String, fuel: Fuel, price: Int, `new`: Boolean,
                  mileage: Option[Int], registration: Option[Date])