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

package uk.gov.hmrc.awrslookup.services

import uk.gov.hmrc.awrslookup.connectors.LookupConnector
import uk.gov.hmrc.awrslookup.models.SearchResult
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait LookupService {

  val connector: LookupConnector

  def lookupAwrsRef(awrsRef: String)(implicit hc: HeaderCarrier): Future[Option[SearchResult]] =
    connector.queryByUrn(awrsRef)

  def lookupByName(queryString: String)(implicit hc: HeaderCarrier): Future[Option[SearchResult]] =
    connector.queryByName(queryString)

}

object LookupService extends LookupService {
  override val connector: LookupConnector = LookupConnector
}
