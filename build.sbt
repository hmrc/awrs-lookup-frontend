/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    ScoverageKeys.coverageExcludedPackages  := "<empty>;app.*;config.*;Reverse.*;.*AuthService.*;models/.data/..*;" +
      "uk.gov.hmrc.BuildInfo;uk.gov.hmrc.awrslookup;prod.*; testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.BuildInfo;views.*; " +
      "audit.*;forms.prevalidation.*;forms.validation.util.*; utils.LoggingUtils;",
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
    scalaVersion := "2.13.14",
    scalacOptions ++= Seq("-feature", "-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s"),
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.hmrcfrontend.views.html.{components => hmrcComponents}"
    ),
    PlayKeys.playDefaultPort := 9511
  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
  .disablePlugins(JUnitXmlReportPlugin)
