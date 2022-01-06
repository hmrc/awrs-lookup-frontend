/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.test

import forms.validation.util.ErrorMessageInterpreter._
import forms.validation.util.{FieldError, MessageLookup, SummaryError}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}

import scala.language.implicitConversions


package object util extends Matchers with FormValidationTestAPI with TestUtilAPI {

  /**
    * function to allow easy attachment of prefix to field ids regardless of whether the
    * prefix is supplied
    *
    * Any class inheriting from this trait can be used as follows
    *
    * Example usage:
    *
    * val prefix = "myprefix"
    * val fieldId = "myId"
    * val newId = prefix attach fieldId
    * // this returns "myprefix.myId"
    */
  implicit def formatId(idPrefix: IdPrefix = None): FieldNameUtilAPI = new FieldNameUtilAPI {
    def attach(fieldId: String): String = if (idPrefix.prefix.nonEmpty) f"${idPrefix.prefix.get}.$fieldId" else fieldId

    def attachToAll(fieldIds: Set[String]): Set[String] = fieldIds.map(fieldId => this.attach(fieldId))
  }


  def assertErrorMessageIsCorrectlyPopulated(errorMessage: MessageLookup)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val message = errorMessage.toString()
    withClue("The message key must be placed in conf/messages: ") {
      message should not be errorMessage.msgKey
    }
    withClue("The message key should be assigned to a value: ") {
      message should not be ""
    }
    withClue("All message arguments must be assigned: ") {
      message.matches("^(.*?)\\{\\d+\\}(.*?)") should not be true
    }
  }

  def assertFormIsValid[T](form: Form[T], testData: Map[String, String])(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val formWithNoErrorsInField = form.bind(testData)
    withClue(f"There shouldn't be any errors in this form:\ntestData=$testData\n") {
      withClue(s"Error found:\n${formWithNoErrorsInField.errors}\n") {
        formWithNoErrorsInField.hasErrors mustBe false
      }
    }
    formWithNoErrorsInField.bind(testData).fold(
      formWithErrors => {},
      formdata => {
        val refilledForm = form.fill(formdata)
        withClue(f"refilled form should be the same as the submitted form:\n") {
          withClue(f"original data\t=\t$testData\nsubmitted form\t=\t${formWithNoErrorsInField.data}\nformdata\t=\t$formdata\n\nrefilledForm\t=\t${refilledForm.data}\n\n") {
            val dset1 = formWithNoErrorsInField.data
            val dset2 = refilledForm.data
            dset1.keys.foreach(key => dset1.get(key) mustBe dset2.get(key))
          }
        }
      }
    )
  }

  def assertFieldError(formWithErrors: Form[_], fieldId: String, expected: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    import forms.prevalidation._
    val fieldError = getFieldErrors(formWithErrors(fieldId), formWithErrors)
    withClue(f"The following test is conducted for the field: '$fieldId'\n") {
      withClue(f"The current form error is'\n${formWithErrors.errors}\n") {
        withClue("An error is expected but the field remained valid: ") {
          fieldError.nonEmpty mustBe true
        }
        withClue("There should be exactly 1 error message per field: ") {
          fieldError.length mustBe 1
        }
        val actualError = fieldError.head
        withClue("The error message for the field differs from the expected:\n") {
          withClue(s"actual:\n$actualError\n${trimBothAndCompressFunc(actualError.toString)}\nexpected:\n$expected\n${trimBothAndCompressFunc(expected.toString)}\n") {
            trimBothAndCompressFunc(actualError.toString) should equal(trimBothAndCompressFunc(expected.toString))
          }
        }
        assertErrorMessageIsCorrectlyPopulated(actualError)
      }
    }
  }

  def assertNotThisFieldError(form: Form[_], fieldId: String, unacceptable: FieldError)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val fieldError = getFieldErrors(form(fieldId), form)
    if (fieldError.nonEmpty) {
      fieldError.head should not equal unacceptable
    }
  }

  def assertHasNoFieldError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val fieldError = getFieldErrors(form(fieldId), form)
    withClue(f"This field should not generate any errors:\nerror=$fieldError\n") {
      fieldError.isEmpty mustBe true
    }
  }

  def assertHasFieldError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val fieldError = getFieldErrors(form(fieldId), form)
    withClue("This field should generate at least one error: ") {
      fieldError.nonEmpty mustBe true
    }
  }

  def assertSummaryError(formWithErrors: Form[_], fieldId: String, expected: SummaryError)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    import forms.prevalidation._
    val summaryErrors = getSummaryErrors(formWithErrors)
    val summaryErrorStrs = summaryErrors.map(err => err.anchor + trimBothAndCompressFunc(err.toString))
    withClue(f"Cannot find the expected summary error message:\nexpected=$expected\nexpected anchor=${expected.anchor}\nin:\nsummaryErrors=$summaryErrors\nsummaryErrors.anchors=${summaryErrors.map(a => f"anchor=${a.anchor}")}") {
      summaryErrorStrs should contain(trimBothAndCompressFunc(expected.anchor + expected.toString))
    }
    assertErrorMessageIsCorrectlyPopulated(expected)
  }

  def assertNotThisSummaryError(form: Form[_], fieldId: String, unacceptable: SummaryError)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val summaryErrors = getSummaryErrors(form)
    summaryErrors should not contain unacceptable
  }

  def assertHasNoAnchorFromSummaryError(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val summaryErrors = getSummaryErrors(form)
    withClue("This field should not generate any errors: ") {
      summaryErrors.foreach(summaryError => summaryError.anchor should not be fieldId)
    }
  }

  def assertFieldCannotBeEmpty(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, fieldIsEmptyExpectation: ExpectedFieldIsEmpty)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    def coreTest(testData: Map[String, String]): Unit = {
      val formWithErrors = form.bind(testData)
      withClue(f"$fieldId should not be empty:\n") {
        formWithErrors.hasErrors mustBe true
        assertFieldError(formWithErrors, fieldId, fieldIsEmptyExpectation.fieldError)
        assertSummaryError(formWithErrors, fieldId, fieldIsEmptyExpectation.summaryError)
      }
    }
    withClue("field is empty test for when the fields are submitted with an empty string\n") {
      val testData = generateFormTestData(preCond, fieldId, "")
      coreTest(testData)
    }
    withClue("field is empty test for when the fields are omitted from submission\n") {
      coreTest(preCond)
    }
  }

  def assertDateFieldCannotBeEmpty(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, fieldIsEmptyExpectation: ExpectedFieldIsEmpty)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val dayFieldId = f"$fieldId.day"
    val monthFieldId = f"$fieldId.month"
    val yearFieldId = f"$fieldId.year"

    def coreTest(testData: Map[String, String]): Unit = {
      val formWithErrors = form.bind(testData)
      withClue(f"$fieldId should not be empty:\n") {
        formWithErrors.hasErrors mustBe true
        assertFieldError(formWithErrors, fieldId, fieldIsEmptyExpectation.fieldError)
        assertSummaryError(formWithErrors, fieldId, fieldIsEmptyExpectation.summaryError)
      }
    }
    withClue("date fields are empty test with all date fields submitted using empty string\n") {
      val testData = generateFormTestData(preCond, dayFieldId, "") ++
        generateFormTestData(monthFieldId, "") ++
        generateFormTestData(yearFieldId, "")
      coreTest(testData)
    }
    withClue("date fields are empty test with all date fields omitted from submission\n") {
      coreTest(preCond)
    }
  }

  def assertFieldCannotBeExceedMaxLength(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, maxLengthExpectationO: MaxLengthOption[ExpectedFieldExceedsMaxLength])(implicit messages: Messages, messagesApi: MessagesApi): Unit =
    maxLengthExpectationO match {
      case MaxLengthIsHandledByTheRegEx() =>
      case MaxLengthDefinition(maxLengthExpectation) =>
        // test with length exceeding the max, this must throw the max length error
        val invalidLen = generateFormTestData(preCond, fieldId,
          generateFieldTestDataInThisFormat(DataFormat("a", maxLengthExpectation.maxLength + 1)))

        withClue(f"$fieldId must not exceed the max length of ${maxLengthExpectation.maxLength}%d:\ntestdata=$invalidLen\n") {
          val formWithErrors = form.bind(invalidLen)
          formWithErrors.hasErrors mustBe true
          assertFieldError(formWithErrors, fieldId, maxLengthExpectation.fieldError)
          assertSummaryError(formWithErrors, fieldId, maxLengthExpectation.summaryError)
        }

        // test with the exact length, this should not throw the max length error (though it can be a different error)
        val validLen = generateFormTestData(preCond, fieldId,
          generateFieldTestDataInThisFormat(DataFormat("a", maxLengthExpectation.maxLength)))

        val formWithNoErrorsInField = form.bind(validLen)
        withClue(f"$fieldId must be valid when length equal to ${maxLengthExpectation.maxLength}%d:\n") {
          assertNotThisFieldError(formWithNoErrorsInField, fieldId, maxLengthExpectation.fieldError)
          assertNotThisSummaryError(formWithNoErrorsInField, fieldId, maxLengthExpectation.summaryError)
        }
    }

  def assertFieldConformsExpectedFormats(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, formatConfig: ExpectedFieldFormat)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    withClue("You must provide at least one case of invalid format : ") {
      formatConfig.invalidFormats.nonEmpty mustBe true
    }
    // test invalids
    formatConfig.invalidFormats.foreach { invalidFormat =>
      val invalidData = generateFormTestData(preCond, fieldId, invalidFormat.invalidCase)
      val formWithErrors = form.bind(invalidData)
      withClue(f"$fieldId should not be valid when its value is:\n'$invalidData':\n") {
        formWithErrors.hasErrors mustBe true
        assertFieldError(formWithErrors, fieldId, invalidFormat.fieldError)
        assertSummaryError(formWithErrors, fieldId, invalidFormat.summaryError)
      }
    }
    // test valids
    formatConfig.validFormats.foreach { validFormat =>
      val validData = generateFormTestData(preCond, fieldId, validFormat.validCase)
      val formWithNoErrorsInField = form.bind(validData)
      withClue(f"$fieldId should be valid when its value is:\n'$validData':\n") {
        assertHasNoFieldError(formWithNoErrorsInField, fieldId)
        assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
      }
    }
  }

  def assertEnumFieldSatisfy(preCond: Map[String, String] = Map())(form: Form[_], fieldId: String, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value])(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    validEnumValues.foreach { enumCase =>
      val validData = generateFormTestData(preCond, fieldId, enumCase.toString)
      val formWithNoErrorsInField = form.bind(validData)
      withClue(f"checking valid enum case:\n${enumCase.toString}\n") {
        assertHasNoFieldError(formWithNoErrorsInField, fieldId)
        assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
      }
    }
    invalidEnumValues.foreach { enumCase =>
      val validData = generateFormTestData(preCond, fieldId, enumCase.toString)
      val formWithErrors = form.bind(validData)
      withClue(f"checking invalid enum case:\n${enumCase.toString}\n") {
        assertHasFieldError(formWithErrors, fieldId)
      }
    }
  }

  def assertFieldIgnoresEmptyConstraintWhen(preCond: Map[String, String])(form: Form[_], fieldId: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val testData = generateFormTestData(preCond, fieldId, "")
    val formWithoutFieldError = form.bind(testData)
    withClue(f"$fieldId should be allowed to be empty when:\n'$preCond'\n") {
      assertHasNoFieldError(formWithoutFieldError, fieldId)
      assertHasNoAnchorFromSummaryError(formWithoutFieldError, fieldId)
    }
  }

  def assertFieldIgnoresMaxLengthConstraintWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, maxLength: Int)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    val invalidLen =
      generateFormTestData(preCond, fieldId,
        generateFieldTestDataInThisFormat(DataFormat("a", maxLength + 1)))
    val formWithoutFieldError = form.bind(invalidLen)
    withClue(f"$fieldId should ignore its max length constraint when:\n'$preCond':\n") {
      assertHasNoFieldError(formWithoutFieldError, fieldId)
      assertHasNoAnchorFromSummaryError(formWithoutFieldError, fieldId)
    }
  }

  def assertFieldIgnoresFormatsConstraitsWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, formatConfig: ExpectedFieldFormat)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    // test invalids
    withClue(f"$fieldId should not conform to any format constraints when:\n'$preCond':\n") {
      formatConfig.invalidFormats.foreach { invalidFormat =>
        val invalidData = generateFormTestData(preCond, fieldId, invalidFormat.invalidCase)
        val formWithNoErrorsInField = form.bind(invalidData)
        withClue(f"$fieldId should not be valid when its value is '$invalidData':\n") {
          assertHasNoFieldError(formWithNoErrorsInField, fieldId)
          assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
        }
      }
      // test valids
      formatConfig.validFormats.foreach { validFormat =>
        val validData = generateFormTestData(preCond, fieldId, validFormat.validCase)
        val formWithNoErrorsInField = form.bind(validData)
        withClue(f"$fieldId should be valid when its value is '$validData':\n") {
          assertHasNoFieldError(formWithNoErrorsInField, fieldId)
          assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
        }
      }
    }
  }

  def assertEnumFieldIgnoresConstraintsWhen(preCond: Map[String, String])(form: Form[_], fieldId: String, validEnumValues: Set[Enumeration#Value], invalidEnumValues: Set[Enumeration#Value])(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
    validEnumValues.foreach { enumCase =>
      val validData = generateFormTestData(preCond, fieldId, enumCase.toString)
      val formWithNoErrorsInField = form.bind(validData)
      assertHasNoFieldError(formWithNoErrorsInField, fieldId)
      assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
    }
    invalidEnumValues.foreach { enumCase =>
      val validData = generateFormTestData(preCond, fieldId, enumCase.toString)
      val formWithNoErrorsInField = form.bind(validData)
      assertHasNoFieldError(formWithNoErrorsInField, fieldId)
      assertHasNoAnchorFromSummaryError(formWithNoErrorsInField, fieldId)
    }
  }

  implicit def singleFieldTestFunctions(fieldIdString: String)(implicit form: Form[_]): ImplicitSingleFieldTestAPI = new ImplicitSingleFieldTestAPI {
    override implicit val fieldId: String = fieldIdString

    def assertFieldIsCompulsory(config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      assertFieldIsCompulsoryWhen(Map[String, String](), config)

    def assertFieldIsCompulsoryWhen(condition: Map[String, String], config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      assertFieldCannotBeEmpty(condition)(form, fieldId, config.fieldIsEmptyExpectation)
      assertFieldCannotBeExceedMaxLength(condition)(form, fieldId, config.maxLengthExpectation)
      assertFieldConformsExpectedFormats(condition)(form, fieldId, config.formatExpectations)
    }

    def assertFieldIsCompulsoryWhen(conditions: Set[Map[String, String]], config: CompulsoryFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertFieldIsCompulsoryWhen(condition, config))

    def assertFieldIsOptional(config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      assertFieldIsOptionalWhen(Map[String, String](), config)

    def assertFieldIsOptionalWhen(condition: Map[String, String], config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      assertFieldCannotBeExceedMaxLength(condition)(form, fieldId, config.maxLengthExpectation)
      assertFieldConformsExpectedFormats(condition)(form, fieldId, config.formatExpectations)
    }

    def assertFieldIsOptionalWhen(conditions: Set[Map[String, String]], config: OptionalFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertFieldIsOptionalWhen(condition, config))

    def assertEnumFieldIsCompulsory(config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      assertEnumFieldIsCompulsoryWhen(Map[String, String](), config)

    def assertEnumFieldIsCompulsoryWhen(condition: Map[String, String], config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      assertFieldCannotBeEmpty(condition)(form, fieldId, config.fieldIsEmptyExpectation)
      assertEnumFieldSatisfy(condition)(form, fieldId, config.validEnumValues, config.invalidEnumValues)
    }

    def assertEnumFieldIsCompulsoryWhen(conditions: Set[Map[String, String]], config: CompulsoryEnumValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertEnumFieldIsCompulsoryWhen(condition, config))

    def assertFieldIsIgnoredWhen(condition: Map[String, String], config: FieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      assertFieldIgnoresEmptyConstraintWhen(condition)(form, fieldId)
      if (config.maxLength.nonEmpty)
        assertFieldIgnoresMaxLengthConstraintWhen(condition)(form, fieldId, config.maxLength.get)
      assertFieldIgnoresFormatsConstraitsWhen(condition)(form, fieldId, config.formatExpectations)
    }

    def assertFieldIsIgnoredWhen(conditions: Set[Map[String, String]], config: FieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertFieldIsIgnoredWhen(condition, config))

    def assertEnumFieldIsIgnoredWhen(condition: Map[String, String], config: EnumFieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      assertFieldIgnoresEmptyConstraintWhen(condition)(form, fieldId)
      assertEnumFieldIgnoresConstraintsWhen(condition)(form, fieldId, config.validEnumValues, config.invalidEnumValues)
    }

    def assertEnumFieldIsIgnoredWhen(conditions: Set[Map[String, String]], config: EnumFieldToIgnore)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertEnumFieldIsIgnoredWhen(condition, config))
  }

  implicit def crossFieldTestFunctions(fieldIdsString: Set[String])(implicit form: Form[_]): ImplicitCrossFieldTestAPI = new ImplicitCrossFieldTestAPI {

    override implicit val fieldIds: Set[String] = fieldIdsString

    private def commonTestForAnswered(testData: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val formWithErrors = form.bind(testData)
      formWithErrors.hasErrors mustBe true
      assertSummaryError(formWithErrors, config.anchor, config.fieldIsEmptyExpectation.summaryError)
      fieldIds.foreach(fieldId => assertFieldError(formWithErrors, fieldId, config.fieldIsEmptyExpectation.fieldError))
    }

    private def commonTestForAnsweredIsIgnored(testData: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val formWithoutErrorsInFields = form.bind(testData)
      assertHasNoAnchorFromSummaryError(formWithoutErrorsInFields, config.anchor)
      fieldIds.foreach(fieldId => assertHasNoFieldError(formWithoutErrorsInFields, fieldId))
    }

    def assertAtLeastOneFieldMustNotBeEmptyWhen(condition: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val testData = generateFormTestData(condition, fieldIds, "")
      withClue(f"The following test is conducted for: at least one of: '$fieldIds' must not be empty:\n") {
        commonTestForAnswered(testData, config)
      }
    }

    def assertAtLeastOneFieldMustNotBeEmptyWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertAtLeastOneFieldMustNotBeEmptyWhen(condition, config))

    def assertAllFieldsCannotBeAnsweredWithInvalidWhen(condition: Map[String, String], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val testData = generateFormTestData(condition, fieldIds, invalidAnswer)
      withClue(f"The following test is conducted for: at least one of: '$fieldIds' must not be answered with $invalidAnswer:\n") {
        commonTestForAnswered(testData, config)
      }
    }

    def assertAllFieldsCannotBeAnsweredWithInvalidWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertAllFieldsCannotBeAnsweredWithInvalidWhen(condition, config, invalidAnswer))

    def assertAtLeastOneFieldMustNotBeEmptyIsIgnoredWhen(condition: Map[String, String], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val testData = generateFormTestData(condition, fieldIds, "")

      val formWithoutErrorsInFields = form.bind(testData)
      withClue(f"The following test is conducted for: at least one of: '$fieldIds' must be filled in constraint should be ignored when:\n'$condition'") {
        assertHasNoAnchorFromSummaryError(formWithoutErrorsInFields, config.anchor)
        fieldIds.foreach(fieldId => assertHasNoFieldError(formWithoutErrorsInFields, fieldId))
      }
    }

    def assertAtLeastOneFieldMustNotBeEmptyIsIgnoredWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertAtLeastOneFieldMustNotBeEmptyIsIgnoredWhen(condition, config))

    def assertAllFieldsCannotBeAnsweredWithInvalidIsIgnoredWhen(condition: Map[String, String], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit = {
      val testData = generateFormTestData(condition, fieldIds, invalidAnswer)
      withClue(f"The following test is conducted for: at least one of: '$fieldIds' must be not be answered with $invalidAnswer constraint should be ignored when:\n'$condition'") {
        commonTestForAnsweredIsIgnored(testData, config)
      }
    }

    def assertAllFieldsCannotBeAnsweredWithInvalidIsIgnoredWhen(conditions: Set[Map[String, String]], config: CrossFieldValidationExpectations, invalidAnswer: String)(implicit messages: Messages, messagesApi: MessagesApi): Unit =
      conditions.foreach(condition => assertAllFieldsCannotBeAnsweredWithInvalidIsIgnoredWhen(condition, config, invalidAnswer))
  }

}
