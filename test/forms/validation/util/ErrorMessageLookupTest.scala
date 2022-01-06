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

package forms.validation.util

import forms.validation.util.ErrorMessageFactory.createErrorMessage
import forms.validation.util.ErrorMessageInterpreter.{defaultSummaryId, getFieldErrors, getSummaryErrors}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.{Constraint, Valid, ValidationResult}
import utils.AwrsUnitTestTraits

class ErrorMessageLookupTest extends AwrsUnitTestTraits {

  case class TestModel(field1: String, field2: String, field3: String)
  def getCCParams(cc: AnyRef): Map[String, String] =
    cc.getClass.getDeclaredFields.foldLeft(Map[String, String]()) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc).toString)
    }.-("$outer")
  val emptyMap: Map[String, String] = getCCParams(TestModel("", "", ""))

  def customConstraint[A, B](extractor: A => B, validation: B => ValidationResult): Constraint[A] =
    Constraint("t1")(model => validation(extractor(model)))

  def isEmpty(fieldValue: String, errorMessageWhenEmpty: ValidationResult): ValidationResult =
    if (fieldValue.isEmpty) {
      errorMessageWhenEmpty
    } else {
      Valid
    }

  val testModelMapping = mapping(
    "field1" -> text,
    "field2" -> text,
    "field3" -> text)(TestModel.apply)(TestModel.unapply)

  "The Message Handler" should {

    val simpleFieldMessage = FieldErrorConfig("empty")
    val singleId = TargetFieldIds("field1")

    val testData: Map[String, String] = emptyMap ++ Map("field1" -> "")

    @inline def specifyField1IsEmptyErrorMessage(testMessage: ValidationResult): Form[TestModel] = {
      val fieldIsEmpty = (field: String) => isEmpty(field, testMessage)
      Form(testModelMapping.verifying(customConstraint((model: TestModel) => model.field1, fieldIsEmpty)))
    }

    "correctly extract the error summary information" when {

      "there are no summary messages defined" in {
        //setup
        val testMessage = createErrorMessage(singleId, simpleFieldMessage)
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
        summarys.length mustBe 1
        val summary: SummaryError = summarys.head
        summary.msgKey mustBe defaultSummaryId(simpleFieldMessage.msgKey)
        summary.msgArgs mustBe MessageArguments()
        summary.anchor mustBe singleId.anchor
      }

      "there are no summary name defined but summary params are defined" in {
        //setup
        val summaryParam = MessageArguments("param1")
        val summaryMessage = SummaryErrorConfig(summaryParam)
        val testMessage = createErrorMessage(singleId, simpleFieldMessage, summaryMessage)
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)

        summarys.length mustBe 1
        val summary: SummaryError = summarys.head
        summary.msgKey mustBe defaultSummaryId(simpleFieldMessage.msgKey)
        summary.msgArgs mustBe summaryParam
        summary.anchor mustBe singleId.anchor
      }

      "there are specific summary name and params defined" in {
        //setup
        val summaryParam = MessageArguments("param1", "param2")
        val summaryMessage = SummaryErrorConfig("custom.summary", summaryParam)
        val testMessage = createErrorMessage(singleId, simpleFieldMessage, summaryMessage)
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
        val summary: SummaryError = summarys.head
        summary.msgKey mustBe summaryMessage.msgKey.get
        summary.msgArgs mustBe summaryParam
        summary.anchor mustBe singleId.anchor
      }
    }

    "correctly extract field error messages" when {

      "only a single id is defined, the field with id should be the only one associated with the error" in {
        //setup
        val testMessage = createErrorMessage(singleId, simpleFieldMessage)
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val errorInfo = field1Errors.head
        errorInfo.msgKey mustBe simpleFieldMessage.msgKey
        val field2Errors = getFieldErrors(formWithErrors("field2"), formWithErrors)
        field2Errors mustBe empty
        val field3Errors = getFieldErrors(formWithErrors("field3"), formWithErrors)
        field3Errors mustBe empty
      }

      "multiple ids are defined, all fields with id should be associated with the error" in {
        //setup
        val multipleId = TargetFieldIds("field1", "field2", "field3")
        val testMessage = createErrorMessage(multipleId, simpleFieldMessage)
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgKey mustBe simpleFieldMessage.msgKey
        val field2Errors = getFieldErrors(formWithErrors("field2"), formWithErrors)
        val field2Error = field2Errors.head
        field2Error.msgKey mustBe simpleFieldMessage.msgKey
        val field3Errors = getFieldErrors(formWithErrors("field3"), formWithErrors)
        val field3Error = field3Errors.head
        field3Error.msgKey mustBe simpleFieldMessage.msgKey
      }
    }

    "message additional parameters are extracted correctly" when {

      val testVarparam = MessageArguments("a", "b", "c", "d")
      val testEmptyVarparam = MessageArguments("", "", "", "")

      "used in field error message" in {
        //setup
        val testMessage = createErrorMessage(singleId, FieldErrorConfig("empty", testVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgArgs mustBe testVarparam
      }

      "used in sumary error message" in {
        //setup
        val testMessage = createErrorMessage(singleId, simpleFieldMessage, SummaryErrorConfig(testVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
        val summary: SummaryError = summarys.head
        summary.msgArgs mustBe testVarparam
      }

      "empty strings used in field error message" in{
        //setup
        val testMessage = createErrorMessage(singleId, FieldErrorConfig("empty", testEmptyVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgArgs mustBe testEmptyVarparam
      }

      "empty strings used in summary error message" in{
        //setup
        val testMessage = createErrorMessage(singleId, simpleFieldMessage, SummaryErrorConfig(testEmptyVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
        val summary: SummaryError = summarys.head
        summary.msgArgs mustBe testEmptyVarparam
      }
    }

    "embedded messages are extracted correctly" when {

      import forms.validation.util.ErrorMessageLookup._

      "simple usage in field error message " in {
        val msgKey = "empty"
        val embeddedArgs = MessageArguments("hello", 2, "world")
        val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
        val testVarparam = MessageArguments(embeddedMessage)
        val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey, testVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgKey mustBe msgKey
        //        field1Error.params mustBe testVarparam
        field1Error.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage))
      }

      "multiple usage in field error message " in {
        val msgKey = "empty"
        val embeddedArgs = MessageArguments("hello", 2, "world")
        val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
        val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
        val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey, testVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgKey mustBe msgKey
        //        field1Error.params mustBe testVarparam
        field1Error.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
      }

      "nested usage in field error message " in {
        val msgKey = "empty"
        val embeddedArgs = MessageArguments("hello", 2, "world")
        val embeddedMessage = EmbeddedMessage("testkey2", MessageArguments(EmbeddedMessage("testkey2", embeddedArgs), EmbeddedMessage("testkey2", embeddedArgs)))
        val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
        val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey, testVarparam))
        val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

        // execute process
        val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

        // evaluate results
        val field1Errors = getFieldErrors(formWithErrors("field1"), formWithErrors)
        val field1Error = field1Errors.head
        field1Error.msgKey mustBe msgKey
        //        field1Error.params mustBe testVarparam
        field1Error.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
      }

      "simple usage in summary error message" when {
        "the default summary key is assumed" in {
          val msgkey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgkey), SummaryErrorConfig(testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe defaultSummaryId(msgkey)
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }

        "there is a specified summary key" in {
          val msgKey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig("test"), SummaryErrorConfig(msgKey, testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe msgKey
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }
      }

      "multiple usage in summary error message " when {
        "the default summary key is assumed" in {
          val msgKey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey), SummaryErrorConfig(testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe defaultSummaryId(msgKey)
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }
        "there is a specified summary key" in {
          val msgKey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig("test"), SummaryErrorConfig(msgKey, testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe msgKey
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }
      }

      "nested usage in summary error message " when {
        "the default summary key is assumed" in {
          val msgKey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", MessageArguments(EmbeddedMessage("testkey2", embeddedArgs), EmbeddedMessage("testkey2", embeddedArgs)))
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey), SummaryErrorConfig(testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe defaultSummaryId(msgKey)
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }

        "there is a specified summary key" in {
          val msgKey = "empty"
          val embeddedArgs = MessageArguments("hello", 2, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", MessageArguments(EmbeddedMessage("testkey2", embeddedArgs), EmbeddedMessage("testkey2", embeddedArgs)))
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig("test"), SummaryErrorConfig(msgKey, testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe msgKey
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage))
          summary.anchor mustBe singleId.anchor
        }
      }

      "complex nested usage in both error messages" when {
        "the default summary key is assumed" in {
          val msgKey = "empty"
          val innerEmbeddedArgs = MessageArguments("good", EmbeddedMessage("testkey"))
          val innerembeddedMessage = EmbeddedMessage("testkey3", innerEmbeddedArgs)
          val embeddedArgs = MessageArguments("hello", innerembeddedMessage, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage, innerembeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey, testVarparam), SummaryErrorConfig(testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe defaultSummaryId(msgKey)
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage), messageLookup(innerembeddedMessage))
          summary.anchor mustBe singleId.anchor
        }

        "there is a specified summary key" in {
          val msgKey = "empty"
          val innerEmbeddedArgs = MessageArguments("good", EmbeddedMessage("testkey"))
          val innerembeddedMessage = EmbeddedMessage("testkey3", innerEmbeddedArgs)
          val embeddedArgs = MessageArguments("hello", innerembeddedMessage, "world")
          val embeddedMessage = EmbeddedMessage("testkey2", embeddedArgs)
          val testVarparam = MessageArguments(embeddedMessage, embeddedMessage, innerembeddedMessage)
          val testMessage = createErrorMessage(singleId, FieldErrorConfig(msgKey, testVarparam), SummaryErrorConfig(msgKey, testVarparam))
          val testModelForm = specifyField1IsEmptyErrorMessage(testMessage)

          // execute process
          val formWithErrors: Form[TestModel] = testModelForm.bind(testData)

          // evaluate results
          val summarys: Seq[SummaryError] = getSummaryErrors(formWithErrors)
          val summary: SummaryError = summarys.head
          summary.msgKey mustBe msgKey
          messageLookup(embeddedMessage)
          summary.msgArgs mustBe MessageArguments(messageLookup(embeddedMessage), messageLookup(embeddedMessage), messageLookup(innerembeddedMessage))
          summary.anchor mustBe singleId.anchor
        }
      }
    }
  }

}
