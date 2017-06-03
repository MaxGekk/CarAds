package carads.backend

import scala.util.Try

trait Storage {
  def createTable: Try[String]

  def getAll(limit: Int): Try[List[Record]]
  def get(id: Int): Try[Record]
  def put(record: Record): Try[Unit]
  def modify(record: Record, attrs: Set[String]): Try[Unit]
  def delete(id: Int): Try[Unit]
}
