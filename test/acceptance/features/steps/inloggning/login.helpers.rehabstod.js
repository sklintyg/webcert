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

/*global logger, JSON, browser, pages */
'use strict';
var helpers = require('../helpers');

var logInAsUserRehabstod = function(userObj, roleName, skipCookieConsent) {
    if (skipCookieConsent) {
        logger.info('Lämnar inte samtycke för kakor');
    }
    logger.info('Loggar in som ' + userObj.forNamn + ' ' + userObj.efterNamn);

    // Fattigmans-kloning av användar-hashen.
    global.user = JSON.parse(JSON.stringify(userObj));

    var login;
    browser.ignoreSynchronization = true;
    browser.get('welcome.html');
    browser.sleep(2000);
    login = pages.welcome.loginByJSON(JSON.stringify(userObj), !skipCookieConsent);
    browser.ignoreSynchronization = false;
    browser.sleep(3000);
    global.user.roleName = roleName;

    return login.then(function() {
        return helpers.injectConsoleTracing();
    });
};

module.exports = {
    logInAsUserRehabstod: logInAsUserRehabstod,
    logInAsUserRoleRehabstod: function(userObj, roleName, skipCookieConsent) {
        logger.info('Loggar in som ' + roleName);
        logger.silly(userObj);
        global.user.roleName = roleName;

        return logInAsUserRehabstod(userObj, roleName, skipCookieConsent).then(function() {
            logger.info('Login default browser successful');
            element.all(by.css('.container h2')).getText().then(function(headings) {
                var index = headings.indexOf('Logga in');
                if (index !== -1) {
                    return expect(headings[index]).to.contain('Logga in');
                }
            });
        });

    }


};
