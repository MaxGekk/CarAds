name := "CarAds"

version := "1.0"

scalaVersion := "2.12.2"

val awsDynamoDbV = "1.11.136"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % awsDynamoDbV
)