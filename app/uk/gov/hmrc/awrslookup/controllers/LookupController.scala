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

package uk.gov.hmrc.awrslookup.controllers

import uk.gov.hmrc.awrslookup.controllers.util.AwrsLookupController
import uk.gov.hmrc.awrslookup.forms.SearchForm._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.awrslookup._
import uk.gov.hmrc.play.frontend.controller.UnauthorisedAction

trait LookupController extends AwrsLookupController {
  def show = UnauthorisedAction.async {
    implicit request =>
      searchForm.bindFromRequest.fold(
        formWithErrors =>
          Ok(views.html.lookup.search(searchForm))
        ,
        query =>
          Ok(views.html.lookup.search(searchForm.fill(query)))
      )
  }
}

object LookupController extends LookupController
