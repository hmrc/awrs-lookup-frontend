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
import forms.test.util._
import utils.AwrsUnitTestTraits

class SearchFormTest extends AwrsUnitTestTraits {

  import SearchForm._

  implicit val form = searchForm.form

  "SearchForm" should {
    "validate and generate the correct error messages" in {
      val fieldId: String = query
      val maxLenError = MaxLengthIsHandledByTheRegEx()
      val invalidFormats = List(ExpectedInvalidFieldFormat("α", fieldId, "search query"))
      val formatError = ExpectedFieldFormat(invalidFormats)

      val expectations = OptionalFieldValidationExpectations(maxLenError, formatError)

      fieldId assertFieldIsOptional expectations
    }
  }

}
