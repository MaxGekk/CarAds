name := "CarAds"

version := "1.0"

scalaVersion := "2.11.9"

val slf4jV = "1.7.21"
val logbackV = "1.0.13"
val awsDynamoDbV = "1.11.136"
val akkaV = "2.3.15"
val sprayV = "1.3.4"
val jsonV = "20160212"
val scalajV = "2.3.0"
val sprayJsonV = "1.3.2"
val commonsV = "2.6"
val json4sV = "3.2.11"

libraryDependencies ++= Seq(
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jcl-over-slf4j" % slf4jV,
  "org.slf4j" % "slf4j-api" % slf4jV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.amazonaws" % "aws-java-sdk-dynamodb" % awsDynamoDbV,
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "io.spray" %% "spray-client" % sprayV,
  "io.spray" %% "spray-testkit" % sprayV,
  "org.json" % "json" % jsonV,
  "org.scalaj" %% "scalaj-http" % scalajV,
  "io.spray" %%  "spray-json" % sprayJsonV,
  "commons-lang" % "commons-lang" % commonsV,
  "org.json4s" %% "json4s-jackson" % json4sV
)