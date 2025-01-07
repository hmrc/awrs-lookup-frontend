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

package connectors

import java.util.UUID
import org.mockito.Mockito._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import exceptions.LookupExceptions
import models.SearchResult
import utils.TestUtils._
import utils.{AwrsUnitTestTraits, LoggingUtils}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.http.client.{RequestBuilder, HttpClientV2}
import scala.concurrent.{ExecutionContext, Future}
import org.mockito.ArgumentMatchers.any
import java.net.URL

class LookupConnectorTest extends AwrsUnitTestTraits {

  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]

  trait ConnectorTest {
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    object TestLookupConnector extends LookupConnector(loggingUtils, mockHttpClient, configuration, servicesConfig)

    def executeGet[A]: Future[A] = {
      val mockGetRequestBuilder: RequestBuilder = mock[RequestBuilder]
      when(mockGetRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockGetRequestBuilder)
      when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(mockGetRequestBuilder)
      mockGetRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
    }
  }

  val urnURL = "/awrs-lookup/query/urn/"
  val nameURL = "/awrs-lookup/query/name/"

  "LookupConnector by urn" should {

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    "lookup an awrs entry when a valid reference number is entered" in new ConnectorTest {
      val expectedResult: Option[SearchResult] = testBusinessSearchResult
      val lookupSuccess: JsValue = SearchResult.formatter.writes(expectedResult.get)
      when(executeGet[HttpResponse]).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess, Map.empty[String, Seq[String]])))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return no awrs entry when a queried reference number is not in the register" in new ConnectorTest {
      val expectedResult: Option[SearchResult] = None
      val response: HttpResponse = HttpResponse(NOT_FOUND, Json.toJson[String](TestLookupConnector.referenceNotFoundString), Map.empty[String, Seq[String]])
      when(executeGet[HttpResponse]).thenReturn(Future.successful(response))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      await(result) mustBe expectedResult
    }

    "return an exception when the middle service is not found" in new ConnectorTest {
      val response: HttpResponse = HttpResponse(NOT_FOUND, "")
      when(executeGet[HttpResponse]).thenReturn(Future.successful(response))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown: InternalServerException = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "URL not found"
    }

    "an exception when invalid json is returned" in new ConnectorTest {
      val invalidJson: JsValue = Json.toJson[String]("""{"key" : "invalid json"}""")
      when(executeGet[HttpResponse]).thenReturn(Future.successful(HttpResponse(OK, invalidJson, Map.empty[String, Seq[String]])))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown: InternalServerException = the[InternalServerException] thrownBy await(result)
      thrown.getMessage mustBe "Invalid json"
    }

    "return 'technical error 400' when a 400 error is returned" in new ConnectorTest {
      val response: HttpResponse = HttpResponse(BAD_REQUEST, "")
      when(executeGet[HttpResponse]).thenReturn(Future.successful(response))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown: LookupExceptions = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 400"
    }

    "return 'technical error 500' when a 500 error is returned" in new ConnectorTest {
      val response: HttpResponse = HttpResponse(INTERNAL_SERVER_ERROR, "")
      when(executeGet[HttpResponse]).thenReturn(Future.successful(response))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown: LookupExceptions = the[LookupExceptions] thrownBy await(result)
      thrown.getMessage mustBe "technical error 500"
    }

    "return an exception when the middle service returns any other status code" in new ConnectorTest {
      val response: HttpResponse = HttpResponse(BAD_GATEWAY, "")
      when(executeGet[HttpResponse]).thenReturn(Future.successful(response))
      val result: Future[Option[SearchResult]] = TestLookupConnector.queryByUrn(testAwrsRef)
      val thrown: InternalServerException = the[InternalServerException] thrownBy await(result)
      thrown.getMessage must include("Unsuccessful return of data. Status code")
    }
  }

  ".encode" should {
    "return a string with replaced UTF values for /" in new ConnectorTest {

      TestLookupConnector.encode(urnURL) mustBe "%2Fawrs-lookup%2Fquery%2Furn%2F"
    }

    "return a string with replaced UTF values for space" in new ConnectorTest {

      TestLookupConnector.encode("awrs-lookup query") mustBe "awrs-lookup%20query"
    }

    "return a string with replaced UTF values for multiple spaces" in new ConnectorTest {

      TestLookupConnector.encode("a b c") mustBe "a%20b%20c"
    }

  }
}
