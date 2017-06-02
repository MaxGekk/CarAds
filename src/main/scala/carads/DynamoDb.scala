package carads

import java.util.Date
import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

trait DynamoDb extends Storage {
  val tableName: String
  val client: AmazonDynamoDB

  def createTable: Try[String] = {
    Try {client.createTable(
      new CreateTableRequest()
        .withTableName(tableName)
        .withKeySchema(List(new KeySchemaElement("id", "HASH")).asJava)
        .withAttributeDefinitions(Seq(
          new AttributeDefinition("id", "N")
        ).asJava)
        .withProvisionedThroughput(
          new ProvisionedThroughput(40000L, 40000L)
        )
    )} map (_.getTableDescription.getTableStatus)
  }

  def put(record: Record): Try[Record] = {
    Try {client.putItem(
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
            "registration" -> new AttributeValue().withN(
              record.registration.getOrElse(new Date(0)).getTime.toString
            )
          ).asJava
        )
        .withReturnValues("ALL_OLD")
      ).getAttributes.asScala
    } flatMap(item2Record)
  }

  def item2Record(item: mutable.Map[String, AttributeValue]): Try[Record] = {
    val rec = for {
      id <- Try { item("id").getN.toInt }
      title <- Try { item("title").getS }
      fuel <- Try { item("fuel").getS match {
        case "Gasoline" => Gasoline()
        case "Diesel" => Diesel()
        case unknown => throw new IllegalArgumentException(s"unknown fuel: $unknown")
      }}
      price <- Try { item("price").getN.toInt }
      isNew <- Try { item("new").getS.toBoolean }
      mileage <- Try { if (isNew) None else Some(item("mileage").getN.toInt) }
      registration <- Try { if (isNew) None else Some(new Date(item("registration").getN.toLong))}
    } yield Record(id, title, fuel, price, isNew, mileage, registration)

    rec
  }

  def get(id: Int): Try[Record] = {
    for {
      item <- Try {
        client.getItem(
          new GetItemRequest().
            withTableName(tableName).
            withKey(
              Map(
                "id" -> new AttributeValue().withN(id.toString)
              ).asJava
            )
        ).getItem
      }
      record <- item2Record(item.asScala)
    } yield record
  }

  def getAll: List[Record] = ???
  def modify(record: Record): Unit = ???
  def delete(id: Int) = ???
}
