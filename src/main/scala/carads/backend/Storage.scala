package carads.backend

import scala.util.Try

trait Storage {
  def createTable: Try[String]

  def getAll(limit: Int): List[Record]
  def get(id: Int): Try[Record]
  def put(record: Record): Try[Record]
  def modify(record: Record, attrs: Set[String]): Try[Record]
  def delete(id: Int): Try[Record]
}
