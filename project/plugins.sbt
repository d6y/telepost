resolvers += Classpaths.typesafeResolver

resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

resolvers += "sonatype.repo" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies <+= sbtVersion(v => v match {
	case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
	case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
	case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
	case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
})

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")





