import sbt.Keys.scalacOptions
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

val appName: String = "awrs-lookup-frontend"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages  := "<empty>;app.*;config.*;Reverse.*;.*AuthService.*;models/.data/..*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.awrslookup;prod.*; testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.BuildInfo;views.*; audit.*;forms.prevalidation.*;forms.validation.util.*; utils.LoggingUtils;",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins : _*)
  .settings(playSettings ++ scoverageSettings : _*)
  .settings( majorVersion := 0 )
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.13.8",
    scalacOptions ++= Seq("-feature", "-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s"),
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.hmrcfrontend.views.html.{components => hmrcComponents}"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false)
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
  .disablePlugins(JUnitXmlReportPlugin)
