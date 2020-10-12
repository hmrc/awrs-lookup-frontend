/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.services

import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.awrslookup.connectors.LookupConnector
import uk.gov.hmrc.awrslookup.models.{AwrsEntry, AwrsStatus, Business, Info, SearchResult}
import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits

import scala.concurrent.Future

class LookupServiceTest extends AwrsUnitTestTraits {

  "LookupService" should {
    
    "call lookup connector and return a the response" in {
      val testAwrs = "XXAW00000123456"

      val data: JsValue = Json.toJson(Business(
        awrsRef = "XXAW00000123456",
        registrationDate = Some("2020-04-01"),
        status = AwrsStatus("Approved"),
        info = Info(businessName = Some("BusinessTest"),
        tradingName = Some("tradeName"),
        fullName = Some("fullName"),
        address = None),
        registrationEndDate = None
        )
      )

      val dataToReturn: Future[Option[SearchResult]] = SearchResult(List(AwrsEntry("Business", data)))

      val mockLookupConnector = mock[LookupConnector]

      when(mockLookupConnector.queryByUrn(Matchers.any())(Matchers.any())).thenReturn(dataToReturn)
      val lookupService = new LookupService(mockLookupConnector)

      val resultWithData = lookupService.lookup(testAwrs)
      resultWithData mustBe dataToReturn

    }
  }
}
