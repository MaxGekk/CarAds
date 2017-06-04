package carads

import carads.backend.{Record, Storage}
import scala.util.{Success, Try}
import scala.collection.concurrent.{TrieMap}

class MemStorage extends Storage {
  val kv = TrieMap[Int, Record]()
  def createTable: Try[String] = { Success("ACTIVE") }

  def getAll(limit: Int): Try[List[Record]] = {
    Try { kv.values.toList }
  }
  def get(id: Int): Try[Record] = {
    Try { kv(id) }
  }
  def put(record: Record): Try[Unit] = {
    Try { kv.put(record.id, record) }
  }
  def modify(record: Record, attrs: Set[String]): Try[Unit] = Try {
    synchronized {
      var r = get(record.id).get
      for (attr <- attrs) attr match {
        case "title" => r = r.copy(title = record.title)
        case "fuel" => r = r.copy(fuel = record.fuel)
        case "price" => r = r.copy(price = record.price)
        case "new" => r = r.copy(`new` = record.`new`)
        case "mileage" => r = r.copy(mileage = record.mileage)
        case "registration" => r = r.copy(registration = record.registration)
      }
      put(r).get
    }
  }
  def delete(id: Int): Try[Unit] = {
    Try { kv.remove(id) }
  }
}
