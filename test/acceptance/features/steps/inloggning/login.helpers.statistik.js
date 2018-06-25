/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/*global logger, JSON, browser */
'use strict';
var helpers = require('../helpers');

function loginByJSON(userJson, giveCookieConsent, self) {
    if (giveCookieConsent) {
        self.disableCookieConsentBanner();
    }

    var loginButton = element(by.id('login_btn'));
    var jsonDisplay = element(by.id('userJsonDisplay'));

    jsonDisplay.clear();
    return jsonDisplay.sendKeys(userJson).then(function() {
        return loginButton.click();
    });

}

var logInAsUserStatistik = function(user, roleName, skipCookieConsent, self) {
    if (skipCookieConsent) {
        logger.info('Lämnar inte samtycke för kakor');
    }
    logger.info('Loggar in som ' + user.fornamn + ' ' + user.efternamn);

    let userObj = {
        fornamn: user.fornamn,
        efternamn: user.efternamn,
        hsaId: user.hsaId,
        vardgivarIdSomProcessLedare: user.vardgivarIdSomProcessLedare,
        vardgivarniva: user.vardgivarniva
    };

    browser.ignoreSynchronization = true;
    return helpers.getUrl('/#/fakelogin').then(function() {
        return loginByJSON(JSON.stringify(userObj), !skipCookieConsent, self);
    }).then(function() {
        browser.ignoreSynchronization = false;
        return helpers.pageReloadDelay();
    });
};

module.exports = {
    logInAsUserStatistik: logInAsUserStatistik,
    logInAsUserRoleStatistik: function(user, roleName, skipCookieConsent) {
        logger.silly(user);
        var self = this;
        return logInAsUserStatistik(user, roleName, skipCookieConsent, self).then(function() {
            logger.info('Login default browser successful');
            var headerboxUserProfile = element(by.css('.header-container'));
            browser.driver.switchTo().alert().then(function(alert) {
                    alert.accept();
                    return expect(headerboxUserProfile.getText()).to.eventually.contain(user.fornamn + ' ' + user.efternamn);

                },
                function(err) {
                    return expect(headerboxUserProfile.getText()).to.eventually.contain(user.fornamn + ' ' + user.efternamn);
                });
        });

    }


};
