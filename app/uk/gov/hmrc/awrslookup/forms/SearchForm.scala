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

package uk.gov.hmrc.awrslookup.forms

import uk.gov.hmrc.awrslookup._
import forms.validation.util.ConstraintUtil.{CompulsoryTextFieldMappingParameter, FieldFormatConstraintParameter, MaxLengthConstraintIsHandledByTheRegEx, OptionalTextFieldMappingParameter}
import forms.validation.util.ErrorMessagesUtilAPI._
import forms.validation.util.MappingUtilAPI._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Valid
import uk.gov.hmrc.awrslookup.models.Query
import prevalidation._
import uk.gov.hmrc.awrslookup.forms.validation.util.ErrorMessageFactory._
import uk.gov.hmrc.awrslookup.forms.validation.util.{FieldErrorConfig, MessageArguments, SummaryErrorConfig, TargetFieldIds}

object SearchForm {

  val query = "query"
  val awrsRefRegEx = "^X[A-Z]AW00000[0-9]{6}$"

  private val queryTargetId = TargetFieldIds(query)
  private val invalidFormatSummaryError = SummaryErrorConfig("awrs.generic.error.character_invalid.summary", MessageArguments())

  val charLenRule = FieldFormatConstraintParameter(
    (name: String) => name.length == 15 match {
      case true =>
        Valid
      case false =>
        createErrorMessage(
          queryTargetId,
          FieldErrorConfig("awrs.search.query.string_length_mismatch"),
          invalidFormatSummaryError)
    }
  )

  val zerosRule = FieldFormatConstraintParameter(
    (name: String) => name.matches("^[a-zA-Z]{4}00000.{6}") match {
      case true =>
        Valid
      case false =>
        createErrorMessage(
          queryTargetId,
          FieldErrorConfig("awrs.search.query.zeros_mismatch"),
          invalidFormatSummaryError)
    }
  )
  val leading4CharRule = FieldFormatConstraintParameter(
    (name: String) => name.matches("^[a-zA-Z]{4}.{11}$") match {
      case true =>
        Valid
      case false =>
        createErrorMessage(
          queryTargetId,
          FieldErrorConfig("awrs.search.query.leading_character_Length_mismatch"),
          invalidFormatSummaryError)
    }
  )

  val leadingXRule = FieldFormatConstraintParameter(
    (name: String) => name.matches("^X.{14}$") match {
      case true =>
        Valid
      case false =>
        createErrorMessage(
          queryTargetId,
          FieldErrorConfig("awrs.search.query.leading_x_mismatch"),
          invalidFormatSummaryError)
    }
  )

  val patternRule = FieldFormatConstraintParameter(
    (name: String) => name.matches(awrsRefRegEx) match {
      case true =>
        Valid
      case false =>
        createErrorMessage(
          queryTargetId,
          FieldErrorConfig("awrs.search.query.default_invalid_urn"),
          invalidFormatSummaryError)
    }
  )

  val compulsoryQueryField = compulsoryText(
    CompulsoryTextFieldMappingParameter(
      empty = simpleFieldIsEmptyConstraintParameter(query, "awrs.search.query.empty"),
      maxLengthValidation = MaxLengthConstraintIsHandledByTheRegEx(),
      formatValidations = Seq(
        charLenRule,
        leading4CharRule,
        leadingXRule,
        zerosRule,
        patternRule
      )
    ))

  val searchValidationForm = Form(mapping(
    query -> compulsoryQueryField
  )(Query.apply)(Query.unapply))

  val searchForm = PreprocessedForm(searchValidationForm)

}
