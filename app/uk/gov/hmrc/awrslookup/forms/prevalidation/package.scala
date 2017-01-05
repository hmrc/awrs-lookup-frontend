/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.forms

import play.api.data.Form

package object prevalidation {

  import TrimOption._

  val defaultTrims = Map[String, TrimOption](
    "query" -> all
  )

  import CaseOption._

  val defaultCases = Map[String, CaseOption](
    "query" -> upper
  )

  val trimAllFunc = (value: String) => value.replaceAll("[\\s]", "")
  val trimBothFunc = (value: String) => value.trim
  val trimBothAndCompressFunc = (value: String) => value.trim.replaceAll("[\\s]{2,}", " ")

  def PreprocessedForm[T](validation: Form[T], trimRules: Map[String, TrimOption] = defaultTrims, caseRules: Map[String, CaseOption] = defaultCases) = {
    val trules = trimRules
    val crules = caseRules
    new PrevalidationAPI[T] {
      override val formValidation = validation
      override val trimRules = trules
      override val caseRules = crules
    }
  }

}
