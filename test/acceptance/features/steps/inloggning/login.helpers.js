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

/*global logger, JSON, browser, pages*/
'use strict';
var helpers = require('../helpers');

var logInAsUser = function(userObj, skipCookieConsent, secondBrowser) {
    if (skipCookieConsent) {
        logger.info('Lämnar inte samtycke för kakor');
    }
    logger.info('Loggar in som ' + userObj.forNamn + ' ' + userObj.efterNamn);

    // Fattigmans-kloning av användar-hashen.
    global.user = JSON.parse(JSON.stringify(userObj));


    //Lägg till en adress för vårdenheten
    global.user.enhetsAdress = {
        postnummer: '66130',
        postort: 'Karlstad',
        postadress: 'Testsvängen 3',
        telefon: '072-9192811'
    };

    global.sessionUsed = false;

    if (!secondBrowser) {
        browser.ignoreSynchronization = true;
        return pages.welcome.get().then(function() {
            return helpers.mediumDelay();
        }).then(function() {
            return pages.welcome.loginByJSON(JSON.stringify(userObj), !skipCookieConsent);
        }).then(function() {
            return helpers.hugeDelay();
        }).then(function() {
            logger.silly('browser.wait(wcHeader.isPresent()');
            return browser.wait(element(by.id('wcHeader')).isPresent(), 10000).then(function(present) {
                return logger.silly('wcHeader is present ' + present);
            });
        }).then(function() {
            return browser.wait(element(by.id('wcHeader')).isDisplayed(), 10000).then(function(displayed) {
                logger.silly('wcHeader is displayed ' + displayed);
                return helpers.smallDelay();
            }, function(err) {
                // Log error but continue. With more delay.
                logger.warn(err);
                return helpers.hugeDelay();
            });
        }).then(function() {
            browser.ignoreSynchronization = false;
            logger.silly('helpers.injectConsoleTracing();');
            return helpers.injectConsoleTracing();
        });
    } else {
        secondBrowser.ignoreSynchronization = true;
        logger.info('Loggar in i andra webbläsaren >>');
        return secondBrowser.get('welcome.html').then(function(result) {
            return secondBrowser.sleep(100).then(function() {
                return pages.welcome.loginByJSON(JSON.stringify(userObj), !skipCookieConsent, secondBrowser);
            }).then(function() {
                return secondBrowser.sleep(100);
            }).then(function() {
                return browser.wait(element(by.id('wcHeader')).isPresent(), 10000).then(function(present) {
                    return logger.silly('wcHeader is present ' + present);
                });
            }).then(function() {
                return browser.wait(element(by.id('wcHeader')).isDisplayed(), 10000).then(function(displayed) {
                    logger.silly('wcHeader is displayed ' + (displayed));
                    return secondBrowser.sleep(100);
                });
            }).then(function() {
                secondBrowser.ignoreSynchronization = false;
                logger.silly('helpers.injectConsoleTracing();');
                return helpers.injectConsoleTracing();
            });
        });
    }
};

module.exports = {
    logInAsUser: logInAsUser,
    logInAsUserRole: function(userObj, roleName, skipCookieConsent, secondBrowser) {
        console.log(userObj);
        global.user.roleName = roleName;

        return logInAsUser(userObj, skipCookieConsent, secondBrowser)
            .then(function() {
                logger.info((secondBrowser) ? 'Login second browser successful' : 'Login default browser successful');
                var wcHeader = secondBrowser ? secondBrowser.findElement(by.id('wcHeader')) : element(by.id('wcHeader'));

                return element(by.id('wcHeader')).getText().then(function(txt) {
                    logger.info('Webcert Header: ' + txt);
                }).then(function() {
                    var promiseArr = [];
                    promiseArr.push(expect(wcHeader.getText()).to.eventually.contain(userObj.forNamn + ' ' + userObj.efterNamn));
                    promiseArr.push(expect(wcHeader.getText()).to.eventually.contain(roleName));

                    return promiseArr;
                });
            });
    }
};
