/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.views

import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty}
import play.api.test.FakeRequest
import uk.gov.hmrc.awrslookup.controllers.LookupController
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.utils.TestUtils.{testBusinessSearchResult, _}
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, HtmlUtils}
import play.api.i18n.Messages

import play.api.test.Helpers._
import uk.gov.hmrc.awrslookup.views.html.helpers.awrs_urn

import scala.concurrent.Future

class LookupViewTest extends AwrsUnitTestTraits with HtmlUtils {
  val mockLookupService: LookupService = mock[LookupService]
  val lookupFailure = Json.parse( """{"reason": "Generic test reason"}""")

  def testRequest(query: Option[String]): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, "/awrs-lookup-frontend" + query.fold("")(q => s"?query=$q"))

  object TestLookupController extends LookupController(environment = environment, configuration = configuration, messagesApi = messagesApi) {
    override val lookupService: LookupService = mockLookupService
  }

  "Lookup Controller " should {

    "display an empty search page landed on for the first time" in {
      val document: Document = TestLookupController.show.apply(testRequest(query = None))
      document.getElementById("search-heading").text shouldBe Messages("awrs.lookup.search.heading")
      document.getElementById("search-lede").text should include(Messages("awrs.lookup.search.lede", Messages("awrs.lookup.search.awrs_urn","","","(",")"), Messages("awrs.lookup.search.isle_of_man", "", "")))

      document.getElementById("query").text shouldBe ""
    }

    "display an awrs entry when a valid reference is entered" in {
      when(mockLookupService.lookupAwrsRef(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(testBusinessSearchResult)))
      val document: Document = TestLookupController.show.apply(testRequest(testAwrsRef))
      val head = testBusinessSearchResult.results.head
      val info = head.info
      document.getElementById("results-heading").text should include(info.tradingName.getOrElse(info.businessName.getOrElse("")))
      document.getElementById("results-heading").text should include(head.status.name)
      document.getElementById("result_awrs_status_label").text should include(Messages("awrs.lookup.results.status_label"))
      document.getElementById("result_awrs_status_detail").text should include(head.status.name)
      document.getElementById("result_awrs_reg_label").text should include(Messages("awrs.lookup.results.reg_number"))
      document.getElementById("result_awrs_reg_detail").text.replaceAll(" ", "") should include(head.awrsRef)
      document.getElementById("result_reg_date_label").text should include(Messages("awrs.lookup.results.date_of_reg"))
      document.getElementById("result_reg_date_detail").text should include(head.registrationDate.get)
      document.getElementById("result_businessName_label").text should include(Messages("awrs.lookup.results.business_name"))
      document.getElementById("result_businessName_detail").text should include(info.businessName.get)
      document.getElementById("result_tradingName_label").text should include(Messages("awrs.lookup.results.trading_name"))
      document.getElementById("result_tradingName_detail").text should include(info.tradingName.get)
      document.getElementById("result_address_label").text should include(Messages("awrs.lookup.results.place_of_bus"))
      document.getElementById("result_address_detail").text should include(info.address.get.addressLine1.get)
    }

    "display a 'No results found' page when a non existent reference is entered" in {
      when(mockLookupService.lookupAwrsRef(Matchers.any())(Matchers.any())).thenReturn(Future.successful(None))
      val document: Document = TestLookupController.show.apply(testRequest(testAwrsRef))
      document.getElementById("no-results-search-term").text.replaceAll(" ", "") should include(Messages("awrs.lookup.search.no_results", testAwrsRef).replaceAll(" ", ""))
    }

    "display a list of awrs entries when a valid reference is entered and multiple are found" in {
      when(mockLookupService.lookupAwrsRef(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Some(testBusinessListSearchResult)))
      val document: Document = TestLookupController.show.apply(testRequest(testAwrsRef))
      val noOfResults = testBusinessListSearchResult.results.size
      document.getElementById("result-count").text should include(Messages("awrs.lookup.results.results_found", noOfResults))
    }
  }
}