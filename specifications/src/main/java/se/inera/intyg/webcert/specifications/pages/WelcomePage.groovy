/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage
import se.inera.intyg.common.specifications.spec.Browser

class WelcomePage extends AbstractPage {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        loginBtn(wait: true, to: [SokSkrivaIntygPage, UnhandledQAPage, AccessDeniedPage, AvtalPage]) { $("#loginBtn") }
    }

    def loginAs(String id) {
        //Having this flag in localStorage will suppress the cookieBanner. (This is what will be set
        //when a user gives consent). We pre-set this before logging in to avoid having to click on that button
        //for every test. (actual testing of the cookiebanner is made with protractor)
        Browser.setLocalStorageValue("wc-cookie-consent-given", "1");
        userSelect = $("#${id}").value();
        loginBtn.click()
    }
}
