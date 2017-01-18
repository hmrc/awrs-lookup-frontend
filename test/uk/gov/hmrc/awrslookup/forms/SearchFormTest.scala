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

import uk.gov.hmrc.awrslookup._
import forms.test.util.{ExpectedInvalidFieldFormat, _}
import forms.validation.util.{FieldError, MessageArguments, SummaryError}
import utils.AwrsUnitTestTraits

class SearchFormTest extends AwrsUnitTestTraits {

  import SearchForm._

  "SearchForm" should {
    implicit val form = searchForm.form

    "validate and generate the correct error messages" in {
      val fieldId: String = query
      val emptyError = ExpectedFieldIsEmpty(fieldId, FieldError("awrs.search.query.empty"))
      val maxLenError = ExpectedFieldExceedsMaxLength(fieldId, "search field", maxQueryLength)
      val summaryError = SummaryError("awrs.generic.error.character_invalid.summary", MessageArguments("search field"), fieldId)
      val invalidFormats = List(ExpectedInvalidFieldFormat("α", fieldId, "search field"))
      val formatError = ExpectedFieldFormat(invalidFormats)

      val expectations = CompulsoryFieldValidationExpectations(emptyError, maxLenError, formatError)

      fieldId assertFieldIsCompulsory expectations
    }

    "allow valid submissions" in {
      assertFormIsValid(form, Map(query -> "XAAW00000123456"))
      assertFormIsValid(form, Map(query -> "XSAW00000123456"))
      assertFormIsValid(form, Map(query -> "XZAW00000999999"))
      assertFormIsValid(form, Map(query -> "XFAW00000000000"))
      assertFormIsValid(form, Map(query -> "My company"))
    }
  }

}
