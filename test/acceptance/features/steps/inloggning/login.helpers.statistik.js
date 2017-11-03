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

/*global logger, JSON, browser, Promise */
'use strict';
// var helpers = require('../helpers');

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

var logInAsUserStatistik = function(userObj, roleName, skipCookieConsent, self) {
    if (skipCookieConsent) {
        logger.info('Lämnar inte samtycke för kakor');
    }
    logger.info('Loggar in som ' + userObj.fornamn + ' ' + userObj.efternamn);

    // Fattigmans-kloning av användar-hashen.
    global.user = JSON.parse(JSON.stringify(userObj));

    var login;
    browser.ignoreSynchronization = true;
    browser.get('/#!/fakelogin');
    browser.sleep(2000);
    login = loginByJSON(JSON.stringify(userObj), !skipCookieConsent, self);
    browser.ignoreSynchronization = false;
    browser.sleep(3000);
    global.user.roleName = roleName;

    return login.then(function() {
        return Promise.resolve();
    });
};

module.exports = {
    logInAsUserStatistik: logInAsUserStatistik,
    logInAsUserRoleStatistik: function(userObj, roleName, skipCookieConsent) {
        console.log(userObj);
        global.user.roleName = roleName;
        var self = this;
        return logInAsUserStatistik(userObj, roleName, skipCookieConsent, self).then(function() {
            logger.info('Login default browser successful');
            var headerboxUserProfile = element(by.css('.headerbox-user-profile'));
            browser.driver.switchTo().alert().then(function(alert) {
                    alert.accept();
                    return expect(headerboxUserProfile.getText()).to.eventually.contain(userObj.fornamn + ' ' + userObj.efternamn);

                },
                function(err) {
                    return expect(headerboxUserProfile.getText()).to.eventually.contain(userObj.fornamn + ' ' + userObj.efternamn);
                });
            // browser.sleep(3000).then(function() {
            //     return expect(headerboxUserProfile.getText()).to.eventually.contain(userObj.forNamn + ' ' + userObj.efterNamn);
            // });
        });

    }


};
