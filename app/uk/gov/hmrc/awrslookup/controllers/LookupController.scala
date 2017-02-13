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

package uk.gov.hmrc.awrslookup.controllers

import javax.inject.Inject

import play.api.data.Form
import play.api.{Configuration, Environment}
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call, Request, Result}
import uk.gov.hmrc.awrslookup._
import uk.gov.hmrc.awrslookup.controllers.util.AwrsLookupController
import uk.gov.hmrc.awrslookup.forms.SearchForm
import uk.gov.hmrc.awrslookup.forms.SearchForm._
import uk.gov.hmrc.awrslookup.forms.prevalidation.PrevalidationAPI
import uk.gov.hmrc.awrslookup.models.{Query, SearchResult}
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.play.frontend.controller.UnauthorisedAction

import scala.concurrent.Future

class LookupController @Inject()(val environment: Environment,
                                 val configuration: Configuration,
                                 val messagesApi: MessagesApi) extends AwrsLookupController {

  private type lookupServiceCall = String => Future[Option[SearchResult]]

  val lookupService: LookupService = LookupService

  private[controllers] def validateFormAndSearch(preValidationForm: PrevalidationAPI[Query], action: Call, lookupCall: lookupServiceCall, fromMulti: Boolean, originalSearchTerm: Option[String])(implicit request: Request[AnyContent]): Future[Result] = preValidationForm.bindFromRequest.fold(
    formWithErrors => Ok(views.html.lookup.search_main(formWithErrors, action)),
    queryForm => {
      val queryString = queryForm.query
      lookupCall(queryString) map {
        case None | Some(SearchResult(Nil)) => Ok(views.html.lookup.search_main(preValidationForm.form, action, searchTerm = queryString, searchResult = SearchResult(Nil)))
        case (Some(result@SearchResult(list))) if list.size > 1 => Ok(views.html.lookup.search_main(searchForm.form, action, searchTerm = queryString, searchResult = result))
        case Some(r: SearchResult) => Ok(views.html.lookup.single_result(searchForm.form, action, r.results.head, searchTerm = queryString, fromMulti = fromMulti, originalSearchTerm = originalSearchTerm))
      }
    }
  )

  def show(fromMulti: Boolean = false) = UnauthorisedAction.async {
    implicit request =>
      val action = controllers.routes.LookupController.show(fromMulti)
      (request.queryString.get(SearchForm.query).isDefined, request.queryString.get("originalSearchTerm").isDefined) match {
        case (true, true) => validateFormAndSearch(preValidationForm = searchForm, action = action, lookupCall = lookupService.lookup, fromMulti = fromMulti, originalSearchTerm = Some(request.queryString.get("originalSearchTerm").get.head))
        case (true, false) => validateFormAndSearch(preValidationForm = searchForm, action = action, lookupCall = lookupService.lookup, fromMulti = fromMulti, originalSearchTerm = None)
        case _ => Ok(views.html.lookup.search_main(searchForm.form, action))
      }
  }

}
