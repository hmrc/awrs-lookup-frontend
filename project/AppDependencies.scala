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

import sbt.*

object AppDependencies {
  import play.sbt.PlayImport.*

  private val bootstrapVersion         = "9.13.0"
  private val playVersion              = "12.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials-play-30"      % "10.1.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup"   %  "jsoup"                  % "1.21.1"         % Test,
    "org.mockito" %  "mockito-core"           % "5.18.0"         % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
