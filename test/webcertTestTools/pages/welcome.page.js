/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/* globals browser, logger */

/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var jsonDisplay = element(by.id('userJsonDisplay'));

module.exports = {
  loginButton: element(by.id('loginBtn')),
  get: function() {
    logger.silly('Går till ' + process.env.WEBCERT_URL + 'welcome.html');
    return browser.get(process.env.WEBCERT_URL + 'welcome.html');
  },
  isAt: function() {
    return this.loginButton.isDisplayed();
  },
  disableCookieConsentBanner: function(secondBrowser) {
    //Having this flag in localStorage will suppress the cookieBanner.(This is what will be set
    //when a user gives consent). We pre-set this before logging in to avoid having to click on that button
    //for every test.
    if (!secondBrowser) {
      return browser.executeScript('window.localStorage.setItem("wc-cookie-consent-given","1");');
    } else {
      return secondBrowser.executeScript('window.localStorage.setItem("wc-cookie-consent-given","1");');
    }
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
    this.loginButton.click();
  },
  loginByName: function(name) {
    this.disableCookieConsentBanner();
    element(by.cssContainingText('option', name)).click();
    this.loginButton.click();
  },
  loginByJSON: function(userJson, giveCookieConsent, secondBrowser) {
    if (giveCookieConsent) {
      this.disableCookieConsentBanner();
    }

    if (!secondBrowser) {
      var loginButton = this.loginButton;
      return jsonDisplay.clear().sendKeys(userJson).then(function() {
        return loginButton.click();
      });
    } else {
      var jsonDisplay2 = secondBrowser.findElement(by.id('userJsonDisplay'));
      var loginButton2 = secondBrowser.findElement(by.id('loginBtn'));
      jsonDisplay2.clear();
      return jsonDisplay2.sendKeys(userJson).then(function() {
        return loginButton2.click();
      });
    }

  }
};
