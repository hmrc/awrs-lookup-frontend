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

package connectors

import java.net.URLEncoder
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import exceptions.LookupExceptions
import forms.prevalidation
import models.SearchResult
import uk.gov.hmrc.http.client.HttpClientV2
import utils.ImplicitConversions._
import utils.LoggingUtils
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class LookupConnector @Inject()(loggingUtils: LoggingUtils,
                                http: HttpClientV2,
                                val runModeConfiguration: Configuration,
                                servicesConfig: ServicesConfig) extends RawResponseReads {

  lazy val middleServiceURL: String = servicesConfig.baseUrl("awrs-lookup")
  lazy val byUrnUrl: String => String = (query: String) => s"""$middleServiceURL/awrs-lookup/query/urn/$query"""

  val referenceNotFoundString = "AWRS reference not found"

  def encode(query: String): String = {
    URLEncoder.encode(query, "UTF-8").replaceAll("\\+", "%20")
  }

  private def responseCore(logRef: String)(response: HttpResponse): Future[Option[SearchResult]] = response.status match {
    case 200 =>
      val responseJson = response.json
      loggingUtils.debug(s"[ ${loggingUtils.auditLookupTxName} - $logRef ] - Json:\n$responseJson\n")
      val parse = Json.fromJson[SearchResult](responseJson)
      if (parse.isSuccess) {
        parse.get
      } else {
        loggingUtils.err(s"[ ${loggingUtils.auditLookupTxName} - $logRef ] - Invalid Json recieved from AWRS-LOOKUP")
        throw new InternalServerException("Invalid json")
      }
    case 404 =>
      response.body match {
        case x if x != null && x.contains(referenceNotFoundString) => None
        case _ =>
          loggingUtils.err(s"[ ${loggingUtils.auditLookupTxName} ] - The remote endpoint has indicated that no data can be found ## ")
          loggingUtils.info(s"[ ${loggingUtils.auditLookupTxName} ] - Query ## $logRef")
          throw new InternalServerException("URL not found")
      }
    case 400 =>
      val error = response.status
      loggingUtils.info(s"[ ${loggingUtils.eventTypeBadRequest} - $logRef ] - Currently experiencing technical difficulties: $error")
      throw new LookupExceptions(s"technical error $error")
    case 500 =>
      val error = response.status
      loggingUtils.info(s"[ ${loggingUtils.auditLookupTxName} - $logRef] - Currently experiencing technical difficulties: $error")
      throw new LookupExceptions(s"technical error $error")
    case status =>
      loggingUtils.err(s"[ ${loggingUtils.auditLookupTxName} - $logRef ] - Unsuccessful return of data. Status code: $status")
      throw new InternalServerException(s"Unsuccessful return of data. Status code: $status")
  }

  def queryByUrn(query: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SearchResult]] = {
    val getURL = byUrnUrl(prevalidation.trimAllFunc(query).toUpperCase)
    http.get(url"$getURL").execute[HttpResponse].flatMap(responseCore(s"ByUrl[ $query ]"))
  }
}
