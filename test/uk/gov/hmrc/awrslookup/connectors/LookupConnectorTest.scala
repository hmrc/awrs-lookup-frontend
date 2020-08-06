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

package uk.gov.hmrc.awrslookup.connectors

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.awrslookup.exceptions.LookupExceptions
import uk.gov.hmrc.awrslookup.models.SearchResult
import uk.gov.hmrc.awrslookup.utils.TestUtils._
import uk.gov.hmrc.awrslookup.utils.{AwrsUnitTestTraits, LoggingUtils}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.SessionId
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

    "lookup an awrs entry when a valid reference number is entered" in {
      val expectedResult: Option[SearchResult] = testBusinessSearchResult
      val lookupSuccess: JsValue = SearchResult.formatter.writes(expectedResult.get)
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess)))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return no awrs entry when a queried reference number is not in the register" in {
      val expectedResult: Option[SearchResult] = None
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND, Json.toJson[String](TestLookupConnector.referenceNotFoundString))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return an exception when the middle service is not found" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "URL not found"
    }

    "an exception when invalid json is returned" in {
      val invalidJson: JsValue = Json.toJson[String]("""{"key" : "invalid json"}""")
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, invalidJson)))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "Invalid json"
    }

    "return 'technical error 400' when a 400 error is returned" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(BAD_REQUEST)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 400"
    }

    "return 'technical error 500' when a 500 error is returned" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(INTERNAL_SERVER_ERROR)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 500"
    }


    "return an exception when the middle service returns any other status code" in {
      val expectedURL = s"""$urnURL$testAwrsRef"""
      implicit val hc = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(BAD_GATEWAY)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage must include("Unsuccessful return of data. Status code")
    }
  }

  "LookupConnector by name" should {

    "lookup an awrs entry when a valid reference number is entered" in {
      val expectedResult: Option[SearchResult] = testBusinessSearchResult
      val lookupSuccess: JsValue = SearchResult.formatter.writes(expectedResult.get)
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess)))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return no awrs entry when a queried reference number is not in the register" in {
      val expectedResult: Option[SearchResult] = None
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND, Json.toJson[String](TestLookupConnector.referenceNotFoundString))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return an exception when the middle service is not found" in {
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "URL not found"
    }

    "an exception when invalid json is returned" in {
      val invalidJson: JsValue = Json.toJson[String]("""{"key" : "invalid json"}""")
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, invalidJson)))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "Invalid json"
    }

    "return an exception when the middle service returns any other status code" in {
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(BAD_GATEWAY)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage must include("Unsuccessful return of data. Status code")
    }

    "return 'technical error 400' when a 400 error is returned" in {
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(BAD_REQUEST)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 400"
    }

    "return 'technical error 500' when a 500 error is returned" in {
      val expectedURL = s"""$nameURL$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(INTERNAL_SERVER_ERROR)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 500"
    }
  }


}
