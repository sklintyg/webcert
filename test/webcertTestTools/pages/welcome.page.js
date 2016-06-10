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

/* globals browser */

/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var loginButton = element(by.id('loginBtn'));
var jsonDisplay = element(by.id('userJsonDisplay'));

module.exports = {
    get: function() {
        return browser.get('welcome.jsp');
    },

    disableCookieConsentBanner: function() {
        //Having this flag in localStorage will suppress the cookieBanner.(This is what will be set
        //when a user gives consent). We pre-set this before logging in to avoid having to click on that button
        //for every test.
        return browser.executeScript('window.localStorage.setItem("wc-cookie-consent-given","1");');
    },
    enableCookieConsentBanner: function() {
        browser.executeScript('window.localStorage.setItem("wc-cookie-consent-given","0");');
    },

    login: function(userId, showCookieBanner) {
        // login id IFV1239877878-104B_IFV1239877878-1042
        // var id = 'IFV1239877878-104B_IFV1239877878-1042';
        if (!showCookieBanner) {
            this.disableCookieConsentBanner();
        } else {
            this.enableCookieConsentBanner();
        }

        element(by.id(userId)).click();
        loginButton.click();
    },
    loginByName: function(name) {
        this.disableCookieConsentBanner();
        element(by.cssContainingText('option', name)).click();
        loginButton.click();
    },
    loginByJSON: function(userJson, giveCookieConsent) {
        if (giveCookieConsent) {
            this.disableCookieConsentBanner();
        }
        return jsonDisplay.clear().sendKeys(userJson).then(function() {
            return loginButton.click();
        });
    }
};
