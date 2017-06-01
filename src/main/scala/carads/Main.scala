package carads

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2._

object Main {
  def main(args: Array[String]): Unit = {
    val storage = new DynamoDb {
      val tableName = "carads"
      val credentials = new BasicAWSCredentials("Fake", "Fake");
      val client = AmazonDynamoDBClientBuilder.standard().
        withCredentials(new AWSStaticCredentialsProvider(credentials)).
        withEndpointConfiguration(new EndpointConfiguration(
          "http://localhost:8000", "local")
        ).
        build()
    }

    storage.createTable
    val orig = Record(1, "Audi A4 Avant", Gasoline(), 30000, true, None, None)
    storage.put(orig)
    val rec = storage.get(1)
    println(s"put = $orig get = $rec")
  }
}
