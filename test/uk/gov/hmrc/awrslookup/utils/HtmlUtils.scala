package uk.gov.hmrc.awrslookup.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html


trait HtmlUtils {

  implicit def soupUtil(html: Html): Document = Jsoup.parse(html.toString)
  implicit def soupUtil2(str: String): Document = Jsoup.parse(str.toString)

  implicit class StringHtmlUtil(str:String) {
    // compress multiple spaces into a single space
    def htmlTrim = str.replaceAll("[\\s]{2,}", " ")
  }

}

object HtmlUtils extends HtmlUtils
