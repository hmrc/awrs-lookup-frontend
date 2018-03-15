import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "awrs-lookup-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playHealthVersion = "2.1.0"
  private val frontendBootstrapVersion = "8.19.0"
  private val playPartialsVersion = "6.1.0"
  private val hmrcTestVersion = "2.4.0"
  private val scalaTestVersion = "2.2.6"
  private val scalaTestPlusPlayVersion = "1.5.1"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "1.10.19"
  private val playLanguageVersion = "3.0.0"

  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.8.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}
