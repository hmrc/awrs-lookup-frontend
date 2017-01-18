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
import play.api.data.Forms._
import uk.gov.hmrc.awrslookup.forms.prevalidation._
import uk.gov.hmrc.awrslookup.forms.validation.util.ConstraintUtil.CompulsoryTextFieldMappingParameter
import uk.gov.hmrc.awrslookup.forms.validation.util.ErrorMessagesUtilAPI._
import uk.gov.hmrc.awrslookup.forms.validation.util.MappingUtilAPI._
import uk.gov.hmrc.awrslookup.forms.validation.util.{MessageArguments, SummaryErrorConfig, TargetFieldIds}
import uk.gov.hmrc.awrslookup.models.Query

object SearchForm {

  val query = "query"
  val awrsRefRegEx = "^X[A-Z]AW00000[0-9]{6}$"
  val maxQueryLength = 140

  val asciiChar32 = 32
  val asciiChar126 = 126
  val asciiChar160 = 160
  val asciiChar255 = 255

  def validText(input: String): Boolean = {
    val inputList: List[Char] = input.toList
    inputList.forall { c =>
      (c >= asciiChar32 && c <= asciiChar126) || (c >= asciiChar160 && c <= asciiChar255)
    }
  }

  private lazy val compulsoryQueryField = compulsoryText(
    CompulsoryTextFieldMappingParameter(
      empty = simpleFieldIsEmptyConstraintParameter(query, "awrs.search.query.empty"),
      maxLengthValidation = genericFieldMaxLengthConstraintParameter(maxQueryLength, query, "search field"),
      formatValidations = genericInvalidFormatConstraintParameter(validText, query, "search field")
    ))

  lazy val searchValidationForm = Form(mapping(
    query -> compulsoryQueryField.toStringFormatter
  )(Query.apply)(Query.unapply))

  lazy val searchForm =
    PreprocessedForm(
      searchValidationForm,
      trimRules = Map(query -> TrimOption.bothAndCompress),
      caseRules = Map())
}
