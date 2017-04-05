/*
 * Copyright 2017 HM Revenue & Customs
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

import java.text.SimpleDateFormat
import java.util.TimeZone

import org.joda.time.DateTime
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.awrslookup.models.{Address, Group, Info}
import uk.gov.hmrc.awrslookup.utils.LetterPairSimilarity

import scala.annotation.tailrec

package object helpers {

  implicit def argConv[T](arg: T): Option[T] = Some(arg)

  implicit def stringToHtml(arg: String): Html = Html(arg)

  implicit def stringToHtml2(arg: String): Option[Html] = Html(arg)

  implicit def stringToHtml3(arg: Option[String]): Option[Html] = arg.map(Html(_))

  def theTime(dateTime: DateTime = DateTime.now()): String = {
    val jsonDateTimeFormat = new SimpleDateFormat("d MMMM yyyy h:mm a")
    jsonDateTimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"))
    jsonDateTimeFormat.format(dateTime.toDate()).replace("AM", "am").replace("PM", "pm")
  }

  def spans(strings: Map[String, Option[String]]): Html =
    Html(
      strings.map {
        case (id, Some(data)) => s"<span id='$id'>$data</span><br/>"
        case _ => ""
      }.view.mkString("")
    )

  def knownName(info: Info): String = info.tradingName match {
    case None | Some("") => info.businessName.fold("")(x => x)
    case Some(name) => name
  }

  def groupLedge(group: Group)(implicit messages: Messages): Html = {
    val groupName = knownName(group.info)
    val text = (list: String) => group.members.size match {
      case 1 => Messages("awrs.lookup.results.group_lede_singular", groupName, list)
      case _ => Messages("awrs.lookup.results.group_lede_plural", groupName, list)
    }

    def tag(info: Info, ind: Int) = s"""<a href=#result_member_${ind}_heading id=result_member_${ind}_lede>${knownName(info)}</a>"""

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

    Html(text(loop(Init, group.members.zipWithIndex) + "."))
  }

  // TODO optimisation, currently we're calling the similarity function multiple times for each element,
  // refactor to reduce the calls
  private[helpers] def infoMatchCoEff(info: Info, searchTerm: String): Double = {
    val tn = info.tradingName.fold("")(x => x)
    val bn = info.businessName.fold("")(x => x)
    val tnMatchPerc = LetterPairSimilarity.compareStrings(tn, searchTerm)
    val bnMatchPerc = LetterPairSimilarity.compareStrings(bn, searchTerm)
    tnMatchPerc > bnMatchPerc match {
      case true => tnMatchPerc
      case false => bnMatchPerc
    }
  }

  def bestMatchName(info: Info, searchTerm: String): String = {
    val tn = info.tradingName.fold("")(x => x)
    val bn = info.businessName.fold("")(x => x)
    val tnMatchPerc = LetterPairSimilarity.compareStrings(tn, searchTerm)
    val bnMatchPerc = LetterPairSimilarity.compareStrings(bn, searchTerm)
    tnMatchPerc > bnMatchPerc match {
      case true => tn
      case false => bn
    }
  }

  private[helpers] def memberWithTheClosestMatch(members: List[Info], searchTerm: String): Info =
    members.sortBy {
      (info: Info) =>
        // sortBy will order in asc, but we need it in desc, 1-x is used here since the percentage will always be
        // between 0 and 1
        1 - infoMatchCoEff(info, searchTerm)
    }.head

  def groupSearchBestMatchInfo(group: Group, searchTerm: String)(implicit messages: Messages): String = {
    val bestMatch = memberWithTheClosestMatch(group.members :+ group.info, searchTerm)
    bestMatch == group.info match {
      case true => bestMatchName(bestMatch, searchTerm)
      case false => Messages("awrs.lookup.results.group_h1_member_of", bestMatchName(bestMatch, searchTerm), knownName(group.info))
    }
  }

  def groupSearchBestMatchAddress(group: Group, searchTerm: String)(implicit messages: Messages): Option[Address] =
    memberWithTheClosestMatch(group.members :+ group.info, searchTerm).address

}
