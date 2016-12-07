package uk.gov.hmrc.awrslookup.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future


trait HtmlUtils {

  implicit def soupUtil(html: Html): Document = Jsoup.parse(html.toString)
  implicit def soupUtil2(str: String): Document = Jsoup.parse(str.toString)
  implicit def soupUtil3(result: Future[Result]) = Jsoup.parse(contentAsString(result))

  implicit class StringHtmlUtil(str:String) {
    // compress multiple spaces into a single space
    def htmlTrim = str.replaceAll("[\\s]{2,}", " ")
  }

}

object HtmlUtils extends HtmlUtils
