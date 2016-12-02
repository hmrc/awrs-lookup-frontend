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

import uk.gov.hmrc.awrslookup.models.AwrsStatus.Pending
import uk.gov.hmrc.awrslookup.models.{Business, Group, Info, SearchResult}
import uk.gov.hmrc.awrslookup.utils.ImplicitConversions._

import scala.concurrent.Future

trait LookupService {

  def lookupAwrsRef(awrsRef: String): Future[Option[SearchResult]] =
    awrsRef match {
      case "XXAW00000123457" =>
        SearchResult(
          List(
            Business("XXAW00000123456", "1 April 2017", Pending, Info("info", "info")),
            Group("XXAW00000123455", "1 April 2017", Pending, List(Info("info", "info"), Info("info2", "info2")))
          ))
      case "XXAW00000123456" => SearchResult(List(Business("XXAW00000123456", "1 April 2017", Pending, Info("info", "info"))))
      case "XXAW00000123455" => SearchResult(List(Group("XXAW00000123455", "1 April 2017", Pending, List(Info("info", "info"), Info("info2", "info2")))))
      case "XXAW00000123454" => SearchResult(List())
      case _ => None
    }

}

object LookupService extends LookupService
