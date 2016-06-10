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

/*global logger, JSON, browser, pages */
'use strict';

module.exports = {
    logInAsUserRole: function(userObj, roleName, skipCookieConsent) {
        if (skipCookieConsent) {
            logger.info('Lämnar inte samtycke för kakor');
        }
        logger.info('Loggar in som ' + userObj.fornamn + ' ' + userObj.efternamn);

        // Fattigmans-kloning av användar-hashen.
        global.user = JSON.parse(JSON.stringify(userObj));

        browser.ignoreSynchronization = true;
        pages.welcome.get();
        browser.sleep(1000);
        pages.welcome.loginByJSON(JSON.stringify(userObj), !skipCookieConsent);

        browser.ignoreSynchronization = false;
        browser.sleep(3000);
        // webcertBasePage.header.getText()
        return expect(element(by.id('wcHeader')).getText()).to.eventually.contain(roleName + ' - ' + userObj.fornamn + ' ' + userObj.efternamn);
    }

};
