/*
 * Copyright 2018 HM Revenue & Customs
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
      val summaryError = (message: String) => SummaryError(message, MessageArguments("search field"), fieldId)
      val invalidFormats = List(
        ExpectedInvalidFieldFormat("α", fieldId, "search field"),
        ExpectedInvalidFieldFormat("XAAW000001234567", FieldError("awrs.search.query.string_length_mismatch"), summaryError("awrs.search.query.string_length_mismatch")),
        ExpectedInvalidFieldFormat("XAAW0000012345", FieldError("awrs.search.query.string_length_mismatch"), summaryError("awrs.search.query.string_length_mismatch")),
        ExpectedInvalidFieldFormat("XAAW00001123456", FieldError("awrs.search.query.zeros_mismatch"), summaryError("awrs.search.query.zeros_mismatch")),
        ExpectedInvalidFieldFormat("XAAW0000012345X", FieldError("awrs.search.query.default_invalid_urn"), summaryError("awrs.search.query.default_invalid_urn")),
        ExpectedInvalidFieldFormat("X0AW00000123456", FieldError("awrs.search.query.default_invalid_urn"), summaryError("awrs.search.query.default_invalid_urn")),
        ExpectedInvalidFieldFormat("XXA000000123456", FieldError("awrs.search.query.default_invalid_urn"), summaryError("awrs.search.query.default_invalid_urn")),
          //when name search is reinstated delete line below
        ExpectedInvalidFieldFormat("Xy company 188555", FieldError("awrs.search.query.default_invalid_urn"), summaryError("awrs.search.query.default_invalid_urn"))

      )
      val formatError = ExpectedFieldFormat(invalidFormats)

      val expectations = CompulsoryFieldValidationExpectations(emptyError, maxLenError, formatError)

      fieldId assertFieldIsCompulsory expectations
    }

    "allow valid submissions" in {
      assertFormIsValid(form, Map(query -> "XAAW00000123456"))
      assertFormIsValid(form, Map(query -> "XSAW00000123456"))
      assertFormIsValid(form, Map(query -> "XZAW00000999999"))
      assertFormIsValid(form, Map(query -> "XFAW00000000000"))
      //when name search is reinstated uncomment lines below
//      assertFormIsValid(form, Map(query -> "My company"))
//      assertFormIsValid(form, Map(query -> "My company 1885"))
//      assertFormIsValid(form, Map(query -> "My 2 company 1885"))
    }
  }

}
