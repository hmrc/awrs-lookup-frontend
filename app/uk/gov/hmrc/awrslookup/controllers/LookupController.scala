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

package uk.gov.hmrc.awrslookup.controllers

import javax.inject.Inject
import play.api.i18n.{Lang, Messages}
import play.api.mvc._
import uk.gov.hmrc.awrslookup._
import uk.gov.hmrc.awrslookup.connectors.RawResponseReads
import uk.gov.hmrc.awrslookup.controllers.util.AwrsLookupController
import uk.gov.hmrc.awrslookup.forms.SearchForm
import uk.gov.hmrc.awrslookup.forms.SearchForm._
import uk.gov.hmrc.awrslookup.forms.prevalidation.PrevalidationAPI
import uk.gov.hmrc.awrslookup.models.{Query, SearchResult}
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.views.html.error_template
import uk.gov.hmrc.awrslookup.views.html.lookup.{search_main, search_no_results, single_result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LookupController @Inject()(mcc: MessagesControllerComponents,
                                 val lookupService: LookupService,
                                 searchMain: search_main,
                                 searchNoResults: search_no_results,
                                 singleResult: single_result,
                                 errorTemplate: error_template) extends AwrsLookupController(mcc) with RawResponseReads {

  private type lookupServiceCall = String => Future[Option[SearchResult]]

  private[controllers] def validateFormAndSearch(preValidationForm: PrevalidationAPI[Query], action: Call,
                                                 lookupCall: lookupServiceCall
                                                 )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val lang: Lang = request.lang
    preValidationForm.bindFromRequest.fold(
      formWithErrors => {
        val query = formWithErrors.data.get("query")
        val err = formWithErrors.errors.head.messages.head.split(".summary#").head
        Ok(searchNoResults(formWithErrors, action, searchTerm = query, errorMessage = err))
      },
      queryForm => {
        val queryString = queryForm.query
        lookupCall(queryString) map {
          case None | Some(SearchResult(Nil)) =>
            Ok(searchNoResults(preValidationForm.form, action, searchTerm = queryString, errorMessage = None))

          case Some(r: SearchResult) =>
            Ok(singleResult(searchForm.form, action, r.results.head, searchTerm = queryString, searchResult = r)) // single result
        }
      }.recover {
        case _ =>
          Ok(errorTemplate(Messages("awrs.error.technical.title"), Messages("awrs.error.technical.heading"), Messages("awrs.error.technical.message")))
      }

    )
  }

  def show(): Action[AnyContent] = Action.async {

    implicit request =>

      val lang: Lang = request.lang
      val action = controllers.routes.LookupController.show()
      if (request.queryString.contains(SearchForm.query)) {
        validateFormAndSearch(preValidationForm = searchForm, action = action, lookupCall = lookupService.lookup)
      } else {
        Ok(searchMain(searchForm.form, action)(request, request2Messages, messagesApi, lang))
      }
  }

}
