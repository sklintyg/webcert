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

/*global browser,Promise,protractor,logger*/
'use strict';
var logInAsUserRole = require('./login.helpers.js').logInAsUserRole;
var helpers = require('../helpers.js');

module.exports = {
    login: function(userObj, url, secondBrowser) {
        return logInAsUserRole(userObj.userObj, userObj.role, false, secondBrowser).then(function(value) {
            return secondBrowser.get(url).then(function() {
                logger.info('Default browser sleep for 5 sec,\t' + new Date());
                return browser.sleep(5000).then(function() {
                    return Promise.resolve();
                });
            });
        });
    },
    changeFields: function(secondBrowser, elementId) {
        logger.info('Default browser done sleeping,\t\t' + new Date());

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
    refreshBrowser: function(secondBrowser) {
        return secondBrowser.driver.getCurrentUrl().then(function(url) {
            secondBrowser.ignoreSynchronization = true;
            return secondBrowser.sleep(2000).then(function() {
                return secondBrowser.driver.navigate().refresh().then(function() {
                    return secondBrowser.sleep(2000).then(function() {
                        return secondBrowser.driver.switchTo().alert().then(function(alert) {
                                alert.accept();
                                secondBrowser.ignoreSynchronization = false;
                                return secondBrowser.driver.get(url);

                            },
                            function(err) {
                                secondBrowser.ignoreSynchronization = false;
                                return secondBrowser.driver.get(url);
                            });
                    });
                });
            });
        });
    },
    closeBrowser: function(forkedBrowser) {
        return forkedBrowser.quit().then(function() {
            if (!forkedBrowser.getSession()) {
                logger.info('Forked browser closed (quit)');
                return Promise.resolve();
            }
        });
    },
    findErrorMsg: function(secondBrowser, elementIds, msg) {
        return secondBrowser.findElement(by.id(elementIds.firstBtn)).sendKeys(protractor.Key.SPACE).then(function() {
            if (elementIds.radioBtn) {
                return secondBrowser.findElement(by.id(elementIds.radioBtn)).sendKeys(protractor.Key.SPACE).then(function() {
                    return secondBrowser.findElement(by.id(elementIds.btnDialog)).sendKeys(protractor.Key.SPACE).then(function() {
                        return secondBrowser.findElement(by.css(elementIds.alertDanger)).then(function(elem) {
                            return browser.sleep(2000).then(function() {
                                return elem.getText().then(function(text) {
                                    return expect(text).to.have.string(msg);
                                });
                            });
                        });
                    });
                });
            } else {
                return secondBrowser.findElement(by.id(elementIds.btnDialog)).sendKeys(protractor.Key.SPACE).then(function() {
                    return secondBrowser.findElement(by.css(elementIds.alertDanger)).then(function(elem) {
                        return browser.sleep(2000).then(function() {
                            return elem.getText().then(function(text) {
                                return expect(text).to.have.string(msg);
                            });
                        });
                    });
                });
            }
        });
    },
    clickModalBtn: function(browser, elementIds) {
        return browser.findElement(by.id(elementIds.firstBtn)).sendKeys(protractor.Key.SPACE).then(function() {
            return browser.findElement(by.id(elementIds.btnDialog)).sendKeys(protractor.Key.SPACE).then(function() {
                return Promise.resolve();
            });

        });
    },

    askNewQuestion: function(forkedBrowser) {
        return forkedBrowser.findElement(by.id('askArendeBtn')).sendKeys(protractor.Key.SPACE).then(function() {
            return forkedBrowser.findElement(by.id('arendeNewModelText')).sendKeys(helpers.randomTextString()).then(function() {
                return forkedBrowser.findElement(by.cssContainingText('option', 'Kontakt')).click().then(function() {
                    return forkedBrowser.findElement(by.id('sendArendeBtn')).sendKeys(protractor.Key.SPACE);
                });
            });
        });
    }

};
