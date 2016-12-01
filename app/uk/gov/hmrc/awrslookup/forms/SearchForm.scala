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

import forms.validation.util.ConstraintUtil.{FieldFormatConstraintParameter, MaxLengthConstraintIsHandledByTheRegEx, OptionalTextFieldMappingParameter}
import forms.validation.util.ErrorMessagesUtilAPI._
import forms.validation.util.MappingUtilAPI._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Valid
import uk.gov.hmrc.awrslookup.models.Query

import prevalidation._

object SearchForm {

  val query = "query"
  val awrsRefRegEx = "^X[A-Z]AW00000[0-9]{6}$"

  val optionalQueryField = optionalText(
    OptionalTextFieldMappingParameter(
      maxLengthValidation = MaxLengthConstraintIsHandledByTheRegEx(),
      formatValidations = FieldFormatConstraintParameter(
        (str: String) =>
          str.matches(awrsRefRegEx) match {
            case true => Valid
            case false => simpleErrorMessage(query, "awrs.search.query.invalid")
          }
      )
    ))

  val searchValidationForm = Form(mapping(
    query -> optionalQueryField
  )(Query.apply)(Query.unapply))

  val searchForm = PreprocessedForm(searchValidationForm)

}
