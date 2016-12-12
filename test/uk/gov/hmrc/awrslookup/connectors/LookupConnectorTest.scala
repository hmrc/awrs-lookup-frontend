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

package uk.gov.hmrc.awrslookup.connectors

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.awrslookup.models.SearchResult
import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits
import uk.gov.hmrc.awrslookup.utils.TestUtils._
import uk.gov.hmrc.play._
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.ws.WSGet
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, InternalServerException}

import scala.concurrent.Future

class LookupConnectorTest extends AwrsUnitTestTraits {

  object TestAuditConnector extends AuditConnector with AppName with RunMode {
    override lazy val auditingConfig = LoadAuditingConfig("auditing")
  }

  class MockHttp extends WSGet with HttpAuditing {
    override val hooks = Seq(AuditingHook)

    override def auditConnector: AuditConnector = TestAuditConnector

    override def appName: String = app.configuration.getString("appName").getOrElse("awrs-lookup-frontend")
  }

  val mockWSHttp = mock[WSGet]

  object TestLookupConnector extends LookupConnector {
    override val http = mockWSHttp
  }

  override def beforeEach {
    super.beforeEach
    reset(mockWSHttp)
  }

  "LookupConnector" should {

    "lookup an awrs entry when a valid reference number is entered" in {
      val expectedResult: Option[SearchResult] = testBusinessSearchResult
      val lookupSuccess: JsValue = SearchResult.formatter.writes(expectedResult.get)
      val expectedURL = s"""/awrs-lookup/query/$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess)))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      await(result) shouldBe expectedResult
    }

    "return no awrs entry when a queried reference number is not in the register" in {
      val expectedResult: Option[SearchResult] = None
      val expectedURL = s"""/awrs-lookup/query/$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND, Json.toJson[String](LookupConnector.referenceNotFoundString))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      await(result) shouldBe expectedResult
    }

    "return an exception when the middle service is not found" in {
      val expectedURL = s"""/awrs-lookup/query/$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(NOT_FOUND)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage shouldBe "URL not found"
    }

    "an exception when invalid json is returned" in {
      val invalidJson: JsValue = Json.toJson[String]("""{"key" : "invalid json"}""")
      val expectedURL = s"""/awrs-lookup/query/$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, invalidJson)))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage shouldBe "Invalid json"
    }

    "return an exception when the middle service returns any other status code" in {
      val expectedURL = s"""/awrs-lookup/query/$testAwrsRef"""
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      val response = HttpResponse(BAD_GATEWAY)
      when(mockWSHttp.GET[HttpResponse](Matchers.endsWith(expectedURL))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(response))
      val result = TestLookupConnector.queryByName(testAwrsRef)
      val thrown = the[InternalServerException] thrownBy await(result)
      thrown.getMessage should include ("Unsuccessful return of data. Status code")
    }

  }

}
