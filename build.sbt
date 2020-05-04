import uk.gov.hmrc._
import DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName: String = "awrs-lookup-frontend"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages  :=
      """<empty>;app.*;config.*;Reverse.*;.*AuthService.*;models/.data/..*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.awrslookup;prod.*;
        |testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.awrslookup.views.*;
        |uk.gov.hmrc.awrslookup.audit.*;uk.gov.hmrc.awrslookup.forms.prevalidation.*;uk.gov.hmrc.awrslookup.forms.validation.util.*;
        |uk.gov.hmrc.awrslookup.utils.LoggingUtils;
        """.stripMargin,
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin,SbtArtifactory) ++ plugins : _*)
  .settings(playSettings ++ scoverageSettings : _*)
  .settings( majorVersion := 0 )
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.12.11",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    parallelExecution in IntegrationTest := false)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))
  .disablePlugins(JUnitXmlReportPlugin)