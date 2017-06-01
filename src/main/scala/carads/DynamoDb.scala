package carads

import java.util.Date

import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._

trait DynamoDb extends Storage {
  val tableName: String
  val client: AmazonDynamoDBAsyncClient

  def createTable: Unit = {
    client.createTable(
      new CreateTableRequest()
        .withTableName(tableName)
        .withKeySchema(List(new KeySchemaElement("id", "HASH")).asJava)
        .withAttributeDefinitions(List(
          new AttributeDefinition("id", "N"),
          new AttributeDefinition("title", "S"),
          new AttributeDefinition("fuel", "S"),
          new AttributeDefinition("price", "N"),
          new AttributeDefinition("new", "N"),
          new AttributeDefinition("mileage", "N"),
          new AttributeDefinition("registration", "S")
        ).asJava)
    )
  }

  def put(record: Record): Unit = {
    client.putItem(
      new PutItemRequest().
        withTableName(tableName).
        withItem(
          Map(
            "id" -> new AttributeValue().withN(record.id.toString),
            "title" -> new AttributeValue().withS(record.title),
            "fuel" -> new AttributeValue().withS(record.fuel.toString),
            "price" -> new AttributeValue().withN(record.price.toString),
            "new" -> new AttributeValue().withS(record.`new`.toString),
            "mileage" -> new AttributeValue().withN(record.mileage.getOrElse(0).toString),
            "registration" -> new AttributeValue().withS(record.registration.getOrElse(new Date(0)).toString)
          ).asJava
        )
    )
  }

  def get(id: Int): Option[Record] = {
    val attrs = client.getItem(
      new GetItemRequest().
        withTableName(tableName).
        withKey(
          Map(
            "id" -> new AttributeValue().withN(id.toString)
          ).asJava
        )
    ).getItem

    Some(Record(
      id = attrs.get("id").getN.toInt,
      title = attrs.get("title").getS,
      fuel = Gasoline(),
      price = attrs.get("price").getN.toInt,
      `new` = false,
      mileage = None,
      registration = None
    ))
  }

  def getAll: List[Record] = ???
  def modify(record: Record): Unit = ???
  def delete(id: Int) = ???
}
