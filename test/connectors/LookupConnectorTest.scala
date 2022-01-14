/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import exceptions.LookupExceptions
import models.SearchResult
import utils.TestUtils._
import utils.{AwrsUnitTestTraits, LoggingUtils}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class LookupConnectorTest extends AwrsUnitTestTraits {

  val mockWSHttp: DefaultHttpClient = mock[DefaultHttpClient]
  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]

  object TestLookupConnector extends LookupConnector(loggingUtils, mockWSHttp, configuration, servicesConfig)

  override def beforeEach {
    super.beforeEach
    reset(mockWSHttp)
  }

  val urnURL = "/awrs-lookup/query/urn/"
  val nameURL = "/awrs-lookup/query/name/"

  "LookupConnector by urn" should {

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "lookup an awrs entry when a valid reference number is entered" in {
      val expectedResult: Option[SearchResult] = testBusinessSearchResult
      val lookupSuccess: JsValue = SearchResult.formatter.writes(expectedResult.get)
      val expectedURL = s"""$urnURL$testAwrsRef"""
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, lookupSuccess, Map.empty[String, Seq[String]])))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return no awrs entry when a queried reference number is not in the register" in {
      val expectedResult: Option[SearchResult] = None
      val expectedURL = s"""$urnURL$testAwrsRef"""
      val response = HttpResponse(NOT_FOUND, Json.toJson[String](TestLookupConnector.referenceNotFoundString), Map.empty[String, Seq[String]])
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return an exception when the middle service is not found" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      val response = HttpResponse(NOT_FOUND, "")
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "URL not found"
    }

    "an exception when invalid json is returned" in {
      val invalidJson: JsValue = Json.toJson[String]("""{"key" : "invalid json"}""")
      val expectedURL = s"""$urnURL$testAwrsRef"""
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, invalidJson, Map.empty[String, Seq[String]])))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "Invalid json"
    }

    "return 'technical error 400' when a 400 error is returned" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      val response = HttpResponse(BAD_REQUEST, "")
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 400"
    }

    "return 'technical error 500' when a 500 error is returned" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      val response = HttpResponse(INTERNAL_SERVER_ERROR, "")
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 500"
    }

    "return an exception when the middle service returns any other status code" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      val response = HttpResponse(BAD_GATEWAY, "")
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage must include("Unsuccessful return of data. Status code")
    }
  }

  ".encode" should {
    "return a string with replaced UTF values for /" in {

      TestLookupConnector.encode(urnURL) mustBe "%2Fawrs-lookup%2Fquery%2Furn%2F"
    }

    "return a string with replaced UTF values for space" in {

      TestLookupConnector.encode("awrs-lookup query") mustBe "awrs-lookup%20query"
    }

    "return a string with replaced UTF values for multiple spaces" in {

      TestLookupConnector.encode("a b c") mustBe "a%20b%20c"
    }

  }
}
