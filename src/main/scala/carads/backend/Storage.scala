package carads.backend

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

trait Storage {
  def createTable: Try[String]

  def getAll(limit: Int): Try[List[Record]]
  def get(id: Int): Try[Record]
  def put(record: Record): Try[Unit]
  def modify(record: Record, attrs: Set[String]): Try[Unit]
  def delete(id: Int): Try[Unit]
}

object Storage {
  val pattern = "yyyy-MM-dd"
  def date2Str(date: Date): String = {
    new SimpleDateFormat(pattern).format(date)
  }
  def str2Date(str: String): Date = {
    new SimpleDateFormat(pattern).parse(str)
  }
}
