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
import play.api.data.validation.Valid
import uk.gov.hmrc.awrslookup.forms.prevalidation._
import uk.gov.hmrc.awrslookup.forms.validation.util.ConstraintUtil.{CompulsoryTextFieldMappingParameter, FieldFormatConstraintParameter}
import uk.gov.hmrc.awrslookup.forms.validation.util.ErrorMessageFactory.createErrorMessage
import uk.gov.hmrc.awrslookup.forms.validation.util.ErrorMessagesUtilAPI._
import uk.gov.hmrc.awrslookup.forms.validation.util.MappingUtilAPI._
import uk.gov.hmrc.awrslookup.forms.validation.util.{FieldErrorConfig, MessageArguments, SummaryErrorConfig, TargetFieldIds}
import uk.gov.hmrc.awrslookup.models.Query

object SearchForm {

  val query = "query"
  val awrsRefRegEx = "^[xX][a-zA-Z][aA][wW]00000[0-9]{6}$"
  private lazy val leading4CharRegex = "^[a-zA-Z]{4}.{11}$"
  private lazy val leadingXRegex = "^[xX].{14}$"
  private lazy val zerosRegex = "^[a-zA-Z]{4}00000.{6}"
  // if the user has entered more than 5 numbers, we assume they were trying to enter a URN
  private lazy val guessUrnRegex = "(.*?[0-9]){6,}".r
  val maxQueryLength = 140

  private lazy val queryTargetId = TargetFieldIds(query)
 // private lazy val invalidFormatSummaryError = SummaryErrorConfig("awrs.generic.error.maximum_length.summary", MessageArguments("search field"))
  private lazy val invalidFormatSummaryError =
    (fieldErr: String) => SummaryErrorConfig(fieldErr + ".summary", MessageArguments("search field"))

  private lazy val invalidQueryFieldError =
    (fieldErr: String) => createErrorMessage(
      queryTargetId,
      FieldErrorConfig(fieldErr),
      invalidFormatSummaryError(fieldErr))

  private lazy val formatRules =
    FieldFormatConstraintParameter(
      (name: String) => {
        trimAllFunc(name) match {
          case trimmedName@_ if !validText(trimmedName) => invalidQueryFieldError("awrs.generic.error.character_invalid")
          case trimmedName@_ if !guessUrnRegex.findFirstIn(trimmedName).isDefined => Valid
          case trimmedName@_ if trimmedName.matches(awrsRefRegEx) => Valid
          case trimmedName@_ if trimmedName.length != 15 => invalidQueryFieldError("awrs.search.query.string_length_mismatch")
          case trimmedName@_ if !trimmedName.matches(leading4CharRegex) => invalidQueryFieldError("awrs.search.query.leading_four_characters_length_mismatch")
          case trimmedName@_ if !trimmedName.matches(leadingXRegex) => invalidQueryFieldError("awrs.search.query.leading_x_mismatch")
          case trimmedName@_ if !trimmedName.matches(zerosRegex) => invalidQueryFieldError("awrs.search.query.zeros_mismatch")
          case _ => invalidQueryFieldError("awrs.search.query.default_invalid_urn")
        }
      }
    )

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
      formatValidations = Seq(formatRules)
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
