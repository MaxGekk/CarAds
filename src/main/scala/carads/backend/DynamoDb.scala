package carads.backend

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
        .withReturnValues(ReturnValue.ALL_OLD)
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
              Map("id" -> new AttributeValue().withN(id.toString)).asJava
            )
        ).getItem
      }
      record <- item2Record(item.asScala)
    } yield record
  }

  def delete(id: Int): Try[Record] = {
    Try {
      client.deleteItem(
        new DeleteItemRequest().
          withTableName(tableName).
          withKey(
            Map("id" -> new AttributeValue().withN(id.toString)).asJava
          ).
          withReturnValues(ReturnValue.ALL_OLD)
      ).getAttributes.asScala
    } flatMap(item2Record)
  }

  def modify(record: Record, attrs: Set[String]): Try[Record] = {
    val tryUpdate = Try {attrs.map( attr => attr -> new AttributeValueUpdate().withValue(
      attr match {
        case "title" => new AttributeValue().withS(record.title)
        case "fuel" => new AttributeValue().withS(record.fuel.toString)
        case "price" => new AttributeValue().withN(record.price.toString)
        case "new" => new AttributeValue().withS(record.`new`.toString)
        case "mileage" => new AttributeValue().withN(record.mileage.getOrElse(0).toString)
        case "registration" => new AttributeValue().withN(
          record.registration.getOrElse(new Date(0)).getTime.toString
        )
      }
    )).toMap.asJava}

    val item = for { update <- tryUpdate } yield client.updateItem(
      new UpdateItemRequest().
        withTableName(tableName).
        withKey(
          Map("id" -> new AttributeValue().withN(record.id.toString)).asJava
        ).
        withAttributeUpdates(update).
        withReturnValues(ReturnValue.ALL_OLD)
    ).getAttributes.asScala

    item flatMap(item2Record)
  }

  def getAll(limit: Int): Try[List[Record]] = {
    val items = Try {
      client.scan(
        new ScanRequest().
          withTableName(tableName).
          withLimit(limit).
          withSelect("ALL_ATTRIBUTES")
      ).getItems.asScala.toList
    }

    items.map(_.flatMap(item => item2Record(item.asScala).toOption))
  }
}
