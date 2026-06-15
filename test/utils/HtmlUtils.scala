/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Result
import play.twirl.api.Html
import scala.concurrent.Future
import play.api.test.Helpers.*
import scala.language.implicitConversions


object HtmlUtils {

  given soupUtil: Conversion[Html, Document] with
    def apply(html: Html): Document = Jsoup.parse(html.toString)

  given soupUtil2: Conversion[String, Document] with
    def apply(str: String): Document = Jsoup.parse(str.toString)
  
  given soupUtil3: Conversion[Future[Result], Document] with
    def apply(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  extension (str: String) {
    // compress multiple spaces into a single space
    def htmlTrim: String = str.replaceAll("[\\s]{2,}", " ")
  }

  extension (res: Future[Result]) {
    def getDocument: Document = Jsoup.parse(contentAsString(res))
  }

}
