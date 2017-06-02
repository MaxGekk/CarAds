package carads

import java.util.Date

import scala.util.Try

sealed trait Fuel
case class Gasoline() extends Fuel {
  override def toString: String = "Gasoline"
}
case class Diesel() extends Fuel {
  override def toString: String = "Diesel"
}

case class Record(id: Int, title: String, fuel: Fuel, price: Int, `new`: Boolean,
                 mileage: Option[Int], registration: Option[Date])
trait Storage {
  def createTable: Unit

  def getAll: List[Record]
  def get(id: Int): Try[Record]
  def put(record: Record): Unit
  def modify(record: Record): Unit
  def delete(id: Int)
}
