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

/*global browser,Promise*/
'use strict';
var logInAsUserRole = require('./login.helpers.js').logInAsUserRole;
var helpers = require('../helpers.js');

module.exports = {
    login: function(userObj, url, secondBrowser) {
        return logInAsUserRole(userObj.userObj, userObj.role, userObj.cookies, secondBrowser).then(function(value) {
            return secondBrowser.get(url).then(function() {
                console.log('Default browser sleep for 5 sec,\t' + new Date());
                return browser.sleep(5000).then(function() {
                    return Promise.resolve();
                });
            });
        });
    },
    changeFields: function(secondBrowser, elementId) {
        console.log('Default browser done sleeping,\t\t' + new Date());

        var customBrowser = browser.findElement(by.id(elementId));
        var randomTxt = helpers.randomTextString();
        return customBrowser.sendKeys(randomTxt).then(function() {
            // Second browser
            var customBrowser1 = secondBrowser.findElement(by.id(elementId));
            var randomTxt1 = helpers.randomTextString();
            return customBrowser1.sendKeys(randomTxt1).then(function() {
                var saveErrorMessage = secondBrowser.findElement(by.binding('viewState.common.error.saveErrorMessage'));
                return expect(saveErrorMessage.getText()).to.eventually.contain('Kan inte spara utkastet.');
            });
        });
    },
    refreshBroswer: function(secondBrowser) {
        return secondBrowser.driver.getCurrentUrl().then(function(url) {
            secondBrowser.ignoreSynchronization = true;
            secondBrowser.sleep(2000);
            return secondBrowser.driver.navigate().refresh().then(function() {
                secondBrowser.sleep(2000);
                return secondBrowser.driver.switchTo().alert().then(function(alert) {
                    alert.accept();
                    secondBrowser.ignoreSynchronization = false;
                    return secondBrowser.driver.get(url);

                });
            });
        });
    },
    closeBrowser: function(forkedBrowser) {
        return forkedBrowser.quit().then(function() {
            if (!forkedBrowser.getSession()) {
                console.log('Forked browser closed (quit)');
                return Promise.resolve();
            }
        });
    }

};
