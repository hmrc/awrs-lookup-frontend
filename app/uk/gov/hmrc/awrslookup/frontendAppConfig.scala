/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup

import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val analyticsToken: Option[String]
  val betaFeedbackUnauthenticatedUrl: String
  val externalReportProblemUrl: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = getConfString(key,throw new Exception(s"Missing configuration key: $key"))

  private val contactFrontendService = baseUrl("contact-frontend")
  private val contactHost = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier = "AWRS-LOOKUP"

  override lazy val analyticsToken: Option[String] = Some(getString(s"google-analytics.token"))
  override lazy val analyticsHost: String = getString(s"google-analytics.host")
  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports?secure=true"

  override lazy val externalReportProblemUrl = s"$contactHost/contact/problem_reports"

  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  //TODO deal with beta banner feedback link
  val callBackURL = loadConfig(s"beta-feedback.callBackURL")
  val feedBackURL = loadConfig(s"beta-feedback.feedBackURL")
  override lazy val betaFeedbackUnauthenticatedUrl = s"$feedBackURL$callBackURL"

}
