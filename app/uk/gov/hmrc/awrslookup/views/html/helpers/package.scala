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

package uk.gov.hmrc.awrslookup.views.html

import org.joda.time.DateTime
import play.twirl.api.Html
import uk.gov.hmrc.awrslookup.models.{Group, Info}

import scala.annotation.tailrec


package object helpers {

  implicit def argConv[T](arg: T): Option[T] = Some(arg)

  implicit def stringToHtml(arg: String): Html = Html(arg)

  implicit def stringToHtml2(arg: String): Option[Html] = Html(arg)

  implicit def stringToHtml3(arg: Option[String]): Option[Html] = arg.map(Html(_))

  def theTime: String = {
    val now = DateTime.now()
    now.toString("dd MMMM yyyy HH:mm ") + now.toString("a").toLowerCase
  }

  def paragraphs(strings: Option[String]*): Html =
    Html(strings.flatten.view.map(x => s"<p>$x</p>").view.mkString(""))

  def knownName(info: Info): String = info.tradingName match {
    case Some(name) => name
    case _ => info.businessName.fold("")(x => x)
  }

  def groupLedge(group: Group): Html = {
    val groupName = knownName(group.info)
    val text = s"For the purposes of AWRS the group members of $groupName are "

    def tag(text: String, href: String) = s"""<a href=#$href>$text</a>"""

    @tailrec
    def loop(current: String, leftOvers: List[(Info, Int)]): String = leftOvers match {
      case Nil => current
      case (h, ind) :: Nil =>
        val kn = knownName(h)
        current + " and " + tag(kn, s"result_member_${ind}_heading")
      case (h, ind) :: t =>
        val kn = knownName(h)
        loop(current = current + tag(kn, s"result_member_${ind}_heading"), leftOvers = t)
    }

    Html(loop(text, group.members.zipWithIndex))
  }

}
