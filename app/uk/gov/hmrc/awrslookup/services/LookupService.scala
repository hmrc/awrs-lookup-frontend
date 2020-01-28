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

import javax.inject.Inject
import uk.gov.hmrc.awrslookup.connectors.LookupConnector
import uk.gov.hmrc.awrslookup.forms.{SearchForm, prevalidation}
import uk.gov.hmrc.awrslookup.models.SearchResult

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class LookupService @Inject()(val connector: LookupConnector) {

  def lookup(queryString: String)(implicit hc: HeaderCarrier): Future[Option[SearchResult]] =
    prevalidation.trimAllFunc(queryString).toUpperCase.matches(SearchForm.awrsRefRegEx) match {
      case true => connector.queryByUrn(queryString)
      case false => connector.queryByName(queryString)
    }

}
