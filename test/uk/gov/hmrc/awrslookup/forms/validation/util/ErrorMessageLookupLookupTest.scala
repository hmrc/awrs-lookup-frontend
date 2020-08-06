/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.forms.validation.util


import uk.gov.hmrc.awrslookup.forms.validation.util.ErrorMessageLookup._
import uk.gov.hmrc.awrslookup.utils.AwrsUnitTestTraits

class ErrorMessageLookupLookupTest extends AwrsUnitTestTraits {

  private val messagesArgumentMarkupRegex = "\\{\\d+\\}"

  // used for configuring the test data
  private def testDataToString(data: Map[String, String]): String =
    data.toList.flatMap(e => List(f"${e._1}%s=${e._2}%s")).mkString("\n")

  private val messagesFileContent =
    Map("testkey1" -> " result1 ",
      "testkey1.summary" -> " result1 ",
      "test.withprefix.testkey2" -> " result2 {0} {1} {2} ",
      "test.withprefix.testkey2.summary" -> " result2 {0} {1} {2} ",
      "testkey3" -> " result {0} {1} ",
      "testkey3.summary" -> " result {0} {1} ")

  // wasn't sure if it's simply `String`.trim that's required in order to match the behaviour of the message lookup
  private def trimString(str: String): String =
    str.trim

  "The Message Handler" should {
    // currently using messages.en as the inputs for these tests
    // better to replace these with mock data somehow
    "provide the correct message lookup functionality" when {
      "there are no embedded messages" when {
        "it is a FieldErrorInfo instance with no additional parameters" in {
          val key: String = "testkey1"
          val testData: MessageLookup = FieldError(key)
          val message: String = messageLookup(testData)
          message mustBe trimString(messagesFileContent(key))
        }
        "it is a FieldErrorInfo instance with additional parameters" in {
          val key: String = "test.withprefix.testkey2"
          val args: Seq[Any] = Seq("hello", 2, "world")
          val testData: MessageLookup = FieldError(key, MessageArguments(args: _*))
          val message: String = messageLookup(testData)
          message mustBe trimString(messagesFileContent(key).replaceAll(messagesArgumentMarkupRegex, "%s").format(args.map(x => x.toString): _*))
        }
        "it is a SummaryErrorInfo instance with no additional parameters" in {
          val key: String = "testkey1"
          val testData: MessageLookup = SummaryError(key, anchor = "anchor")
          val message: String = messageLookup(testData)
          message mustBe trimString(messagesFileContent(key))
        }
        "it is a SummaryErrorInfo instance with additional parameters" in {
          val key: String = "test.withprefix.testkey2"
          val args: Seq[Any] = Seq("hello", 2, "world")
          val testData: MessageLookup = SummaryError(key, MessageArguments(args: _*), "anchor")
          val message: String = messageLookup(testData)
          message mustBe trimString(messagesFileContent(key).replaceAll(messagesArgumentMarkupRegex, "%s").format(args.map(x => x.toString): _*))
        }
      }

      "there are embedded messages" when {
        "it is a FieldErrorInfo instance" in {
          val outterkey: String = "testkey3"
          val embeddedKey: String = "test.withprefix.testkey2"
          val embededArgs: Seq[Any] = Seq("hello", 2, "world")
          // embedded message is equiv to "result2 %s %s %s".format("hello", 2, "world")
          val embedded: EmbeddedMessage = EmbeddedMessage(embeddedKey, MessageArguments(embededArgs: _*))
          val testData: MessageLookup = FieldError(outterkey, MessageArguments(embedded))
          val message: String = messageLookup(testData)

          val expectedEmbedded = trimString(messagesFileContent(embeddedKey).replaceAll(messagesArgumentMarkupRegex, "%s").format(embededArgs.map(x => x.toString): _*))
          // intentionally leaving the second arg as {1}
          val expectedOutter = messagesFileContent(outterkey).replaceFirst(messagesArgumentMarkupRegex, "%s").format(expectedEmbedded)
          message mustBe trimString(expectedOutter)
        }
        "it is a SummaryErrorInfo instance" in {
          val outterkey: String = "testkey3"
          val embeddedKey: String = "test.withprefix.testkey2"
          val embededArgs: Seq[Any] = Seq("hello", 2, "world")
          // embedded message is equiv to "result2 %s %s %s".format("hello", 2, "world")
          val embedded: EmbeddedMessage = EmbeddedMessage(embeddedKey, MessageArguments(embededArgs: _*))
          val testData: MessageLookup = SummaryError(outterkey, MessageArguments(embedded), "anchor")
          val message: String = messageLookup(testData)

          val expectedEmbedded = trimString(messagesFileContent(embeddedKey).replaceAll(messagesArgumentMarkupRegex, "%s").format(embededArgs.map(x => x.toString): _*))
          // intentionally leaving the second arg as {1}
          val expectedOutter = messagesFileContent(outterkey).replaceFirst(messagesArgumentMarkupRegex, "%s").format(expectedEmbedded)
          message mustBe trimString(expectedOutter)
        }
      }
    }
  }
}
