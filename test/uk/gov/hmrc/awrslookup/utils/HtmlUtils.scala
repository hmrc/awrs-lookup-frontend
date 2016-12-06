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

package uk.gov.hmrc.awrslookup.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Result
import play.twirl.api.Html

import scala.concurrent.Future
import play.api.test.Helpers._


trait HtmlUtils {

  implicit def soupUtil(html: Html): Document = Jsoup.parse(html.toString)

  implicit def soupUtil2(str: String): Document = Jsoup.parse(str.toString)

  implicit class StringHtmlUtil(str: String) {
    // compress multiple spaces into a single space
    def htmlTrim = str.replaceAll("[\\s]{2,}", " ")
  }

  implicit class FutureResultUtil(res: Future[Result]) {
    def getDocument: Document = Jsoup.parse(contentAsString(res))
  }

}

object HtmlUtils extends HtmlUtils
