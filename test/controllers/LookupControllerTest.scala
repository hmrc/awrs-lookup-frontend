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

package controllers

import filters.AwrsLookupRateLimitFilter
import org.jsoup.nodes.Element
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.LookupService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TestUtils.*
import utils.AwrsUnitTestTraits
import utils.HtmlUtils.*
import views.html.error_template
import views.html.lookup.{search_main, search_no_results, single_result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LookupControllerTest extends AwrsUnitTestTraits  {
  val mockLookupService: LookupService = mock[LookupService]
  val lookupFailure: JsValue = Json.parse( """{"reason": "Generic test reason"}""")
  val searchMain: search_main = app.injector.instanceOf[search_main]
  val searchNoResults: search_no_results = app.injector.instanceOf[search_no_results]
  val singleResult: single_result = app.injector.instanceOf[single_result]
  val errorTemplate: error_template = app.injector.instanceOf[error_template]
  val rateLimitFilter: AwrsLookupRateLimitFilter = new AwrsLookupRateLimitFilter(servicesConfig)

  object TestLookupController extends LookupController(mcc, mockLookupService, searchMain, searchNoResults, singleResult, errorTemplate, rateLimitFilter)

  "Lookup Controller " should {

    "in show, lookup awrs entry when passed a valid awrs reference" in {
      when(mockLookupService.lookup(any())(using any(), any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val result = TestLookupController.show().apply(FakeRequest())
      status(result) mustBe OK
    }

    "render a technical error" when {
      "an exception is received from the lookup call" in {
        when(mockLookupService.lookup(any())(using any(), any())).thenReturn(Future.failed(new Exception("failed")))
        val html = TestLookupController.show()(FakeRequest("GET", "check-the-awrs-register/?query=XXAW00000123554")).getDocument

        html.title() mustBe "Sorry, we are experiencing technical difficulties - GOV.UK"
      }
    }

    "return 429 when too many requests are send" ignore  {
      val servicesConfigMock = mock[ServicesConfig]
      val mccInstance: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
      val rateLimitFilterInstance: AwrsLookupRateLimitFilter = new AwrsLookupRateLimitFilter(servicesConfigMock)

      val sut = new LookupController(mccInstance, mockLookupService, searchMain, searchNoResults, singleResult, errorTemplate, rateLimitFilterInstance)

      // ToDo Still depends on values in application.conf
      when(servicesConfigMock.getString("ratelimit-filter.enabled")).thenReturn(true)
      when(servicesConfigMock.getString("ratelimit-filter.bucketSize")).thenReturn("1")
      when(servicesConfigMock.getString("ratelimit-filter.ratePerSecond")).thenReturn("0.001")
      val results = for (_ <- 0 until 100) yield sut.show()(FakeRequest())

      val statuses = await(Future.sequence(results)).map(_.header.status)

      statuses.contains(Status.TOO_MANY_REQUESTS) mustBe true
    }
  }

  "lookup routes" should {

    def callLookupFrontEndAndReturnSummaryError(query: Option[String]): Element = {
      val qString = query match {
        case Some(q) => "?query=" + q
        case _ => ""
      }
      val oResult = route(app, FakeRequest(GET, "/check-the-awrs-register" + qString))
      oResult mustBe Symbol("defined")
      val result = oResult.get
      status(result) mustBe OK
      val doc = result.getDocument
      doc.getElementById("no-results-search-term")
    }

    "do not show error if there is no query string" in {
      callLookupFrontEndAndReturnSummaryError(None) mustBe null
    }

    "show error if the query field is empty" in {
      callLookupFrontEndAndReturnSummaryError(Option("")).text mustBe Messages("awrs.search.query.empty.summary")
    }
  }
}
