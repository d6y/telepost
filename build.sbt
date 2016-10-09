name := "telepost"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"        %% "scala-xml"        % "1.0.4",
  "com.github.scala-incubator.io" %% "scala-io-core"    % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file"    % "0.4.3",
  "javax.mail"                    % "mail"              % "1.4",
  "com.amazonaws"                 % "aws-java-sdk-s3"   % "1.11.41",
  "com.amazonaws"                 % "aws-java-sdk-core" % "1.11.41",
  "net.coobird"                   % "thumbnailator"     % "0.4.8"
)

// http://www.scala-sbt.org/0.13/docs/Forking.html

fork := true

mainClass := Some("Main")

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
