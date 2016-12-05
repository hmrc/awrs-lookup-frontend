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

import play.api.libs.json.Json
import uk.gov.hmrc.awrslookup.models.SearchResult
import uk.gov.hmrc.awrslookup.{FrontendAuditConnector, WSHttp}
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http._

import scala.concurrent.Future
import uk.gov.hmrc.awrslookup.utils.ImplicitConversions._
import scala.concurrent.ExecutionContext.Implicits.global


trait LookupConnector extends ServicesConfig with RawResponseReads with LoggingUtils {

  val http: HttpGet with HttpPost with HttpPut = WSHttp
  lazy val middleServiceURL = baseUrl("awrs-lookup")

  val referenceNotFoundString = "AWRS reference not found"

  def sendQuery(query: String)(implicit hc: HeaderCarrier): Future[Option[SearchResult]] = {
    val getURL = s"""$middleServiceURL/awrs-lookup/query/$query"""
    http.GET(getURL) map {
      response =>
        response.status match {
          case 200 =>
            val responseJson = response.json
            debug(s"[ $auditLookupTxName - $query ] - Json:\n$responseJson\n")
            val parse = Json.fromJson[SearchResult](responseJson)
            parse.isSuccess match {
              case true => parse.get
              case _ =>
                err(s"[ $auditLookupTxName - $query ] - Invalid Json recieved from AWRS-LOOKUP")
                throw new InternalServerException("Invalid json")
            }
          case 404 =>
            response.body.equals(referenceNotFoundString) match {
              case true => None
              case _ =>
                err(s"[ $auditLookupTxName ] - The remote endpoint has indicated that no data can be found ## ")
                info(s"[ $auditLookupTxName ] - Query ## $query")
                throw new InternalServerException("URL not found")
            }
          case status =>
            err(s"[ $auditLookupTxName - $query ] - Unsuccessful return of data. Status code: $status")
            throw new InternalServerException(s"Unsuccessful return of data. Status code: $status")
        }
    }
  }

}

object LookupConnector extends LookupConnector {

  override val appName = "awrs-lookup-frontend"
  override val audit: Audit = new Audit(AppName.appName, FrontendAuditConnector)

}
