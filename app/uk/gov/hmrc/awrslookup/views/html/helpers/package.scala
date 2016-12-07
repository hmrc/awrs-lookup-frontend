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
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.awrslookup.models.{Group, Info}

import scala.annotation.tailrec

package object helpers {

  implicit def argConv[T](arg: T): Option[T] = Some(arg)

  implicit def stringToHtml(arg: String): Html = Html(arg)

  implicit def stringToHtml2(arg: String): Option[Html] = Html(arg)

  implicit def stringToHtml3(arg: Option[String]): Option[Html] = arg.map(Html(_))

  def theTime(time: DateTime = DateTime.now()): String =
    time.toString("dd MMMM yyyy hh:mm ") + time.toString("a").toLowerCase

  def paragraphs(strings: Map[String, Option[String]]): Html =
    Html(s"<p>${
      strings.map(
        x => x match {
          case (id, Some(data)) => s"<p id='${id}'>${data}</p>"
          case _ => ""
        }
      ).view.mkString("")
    }</p>")

  def knownName(info: Info): String = info.tradingName match {
    case Some(name) => name
    case _ => info.businessName.fold("")(x => x)
  }

  def groupLedge(group: Group)(implicit messages: Messages): Html = {
    val groupName = knownName(group.info)
    val text = (list: String) => group.members.size match {
      case 1 => Messages("awrs.lookup.results.group_lede_singular", groupName, list)
      case _ => Messages("awrs.lookup.results.group_lede_plural", groupName, list)
    }

    def tag(info: Info, ind: Int) = s"""<a href=#result_member_${ind}_heading>${knownName(info)}</a>"""

    val Init = ""

    @tailrec
    def loop(current: String, leftOvers: List[(Info, Int)]): String = leftOvers match {
      case Nil => current
      case (info, ind) :: Nil =>
        current + (current match {
          case Init => tag(info, ind)
          case _ => " and " + tag(info, ind)
        })
      case (info, ind) :: t =>
        loop(
          current = current + (current match {
            case Init => tag(info, ind)
            case _ => ", " + tag(info, ind)
          }),
          leftOvers = t
        )
    }

    Html(text(loop(Init, group.members.zipWithIndex)))
  }

}
