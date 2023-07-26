/*
 * Copyright 2023 HM Revenue & Customs
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

package views

import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import controllers.LookupController
import services.LookupService
import utils.TestUtils.{testBusinessSearchResult, _}
import utils.{AwrsUnitTestTraits, HtmlUtils}
import views.html.error_template
import views.html.lookup.{search_main, search_no_results, single_result}

import scala.concurrent.Future

class LookupViewTest extends AwrsUnitTestTraits with HtmlUtils {
  val mockLookupService: LookupService = mock[LookupService]
  val lookupFailure: JsValue = Json.parse( """{"reason": "Generic test reason"}""")
  val searchMain: search_main = app.injector.instanceOf[search_main]
  val searchNoResults: search_no_results = app.injector.instanceOf[search_no_results]
  val singleResult: single_result = app.injector.instanceOf[single_result]
  val errorTemplate: error_template = app.injector.instanceOf[error_template]

  def testRequest(query: Option[String]): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, "/awrs-lookup-frontend" + query.fold("")(q => s"?query=$q"))

  object TestLookupController extends LookupController(mcc, mockLookupService , searchMain, searchNoResults, singleResult, errorTemplate)


  "Lookup Controller " should {
    
    "display an empty search page landed on for the first time" in {
      val document: Document = TestLookupController.show().apply(testRequest(query = None))

      document.title mustBe Messages("awrs.lookup.search.page_title")
      document.select("h1").text mustBe Messages("awrs.lookup.search.heading")
      document.getElementById("query-hint").text must include(Messages("awrs.lookup.search.lede", Messages("awrs.lookup.search.awrs_urn","","","(","),")))
      document.getElementById("query").text mustBe ""
    }

    "display an awrs entry when a valid reference is entered" in {
      when(mockLookupService.lookup(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val document: Document = TestLookupController.show().apply(testRequest(testAwrsRef))
      val head = testBusinessSearchResult.results.head
      val info = head.info

      document.title mustBe Messages("awrs.lookup.results.page_title_single")
      document.getElementById("results-heading").text must include(info.tradingName.getOrElse(info.businessName.getOrElse("")))
      document.getElementById("result_awrs_status_label").text must include(Messages("awrs.lookup.results.status_label"))
      document.getElementById("result_awrs_status_detail").text must include(Messages("awrs.generic.status." + head.status.name.toLowerCase))
      document.getElementById("result_awrs_reg_label").text must include(Messages("awrs.lookup.results.URN"))
      document.getElementById("result_awrs_reg_detail").text.replaceAll(" ", "") must include(head.awrsRef)
      document.getElementById("result_reg_date_label").text must include(Messages("awrs.lookup.results.date_of_reg"))
      document.getElementById("result_reg_date_detail").text must include(head.registrationDate.get)
      document.getElementById("result_businessName_label_result").text must include(Messages("awrs.lookup.results.business_name"))
      document.getElementById("result_businessName_detail_result").text must include(info.businessName.get)
      document.getElementById("result_address_label_result").text must include(Messages("awrs.lookup.results.place_of_bus"))
      document.getElementById("result_address_detail_result").text must include(info.address.get.addressLine1.get)
      document.getElementById("search-date").text must include(Messages("awrs.lookup.results.search_date"))
    }

    "display a 'No results found' page when a non existent reference is entered" in {
      when(mockLookupService.lookup(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      val document: Document = TestLookupController.show().apply(testRequest(testAwrsRef))
      document.title mustBe Messages("awrs.lookup.results.page_title_no_results")
      document.getElementById("not-found").text must include(Messages("awrs.lookup.search.not_found"))
    }

  }
}
