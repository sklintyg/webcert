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

/* globals protractor, intyg, browser, logger, Promise */

'use strict';

var helpers = require('./helpers');
var intygURL = helpers.intygURL;

module.exports = function() {

    this.Given(/^ska jag se en knapp med texten "([^"]*)"$/, function(btnTxt) {
        return expect(element(by.id('ersattBtn')).getText()).to.eventually.equal(btnTxt);
    });

    this.Given(/^jag klickar på ersätta knappen$/, function() {
        return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^om jag klickar på ersätta knappen så ska det finnas en avbryt\-knapp med texten "([^"]*)"$/, function(btnText) {
        return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE).then(function() {
            return element(by.css('.modal-dialog')).getText().then(function(modalText) {
                return expect(modalText).to.contain(btnText);
            });
        });
    });

    this.Given(/^jag klickar på ersätta knappen och ersätter intyget$/, function() {
        global.ersattintyg = {};
        global.ersattintyg.id = intyg.id;
        global.ersattintyg.typ = intyg.typ;

        return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE).then(function() {
            return element(by.id('button1ersatt-dialog')).sendKeys(protractor.Key.SPACE).then(function() {
                logger.info('Clicked ersätt button');
            });
        });
    });

    this.Given(/^jag går tillbaka till det ersatta intyget$/, function() {
        return browser.sleep(4000).then(function() {
            var url = intygURL(global.ersattintyg.typ, global.ersattintyg.id);
            return browser.get(url).then(function() {
                logger.info('Går till url: ' + url);
            });
        });
    });

    this.Given(/^ska jag se en texten "([^"]*)" som innehåller en länk till det ersatta intyget$/, function(replacedMessage) {
        var replaceMsg = element(by.id('wc-intyg-replaced-message'));
        replaceMsg.isPresent().then(function(isPresent) {
            if (isPresent) {
                return expect(replaceMsg.getText()).to.eventually.contain(replacedMessage);
            } else {
                return expect(element(by.id('intyg-already-replaced-warning')).getText()).to.eventually.contain(replacedMessage);
            }

        });
    });

    this.Given(/^ska meddelandet som visas innehålla texten "([^"]*)"$/, function(modalMsg) {
        return expect(element(by.css('.modal-body')).getText()).to.eventually.contain(modalMsg);
    });

    this.Given(/^ska det( inte)? finnas knappar för "([^"]*)"$/, function(inte, buttons) {
        buttons = buttons.split(',');
        var shouldBePresent = typeof(inte) === 'undefined';
        var promiseArr = [];
        buttons.forEach(function(button) {
            if ('skicka' === button) {
                promiseArr.push(expect(element(by.id('sendBtn')).isPresent()).to.become(shouldBePresent)
                    .then(function(val) {
                        logger.info('OK - sendBtn - present: ' + shouldBePresent);
                    }, function(val) {
                        throw ('NOK - sendBtn - expected isPresent to be:' + shouldBePresent);
                    }));
            } else if ('kopiera' === button) {
                promiseArr.push(expect(element(by.id('copyBtn')).isPresent()).to.become(shouldBePresent)
                    .then(function(val) {
                        logger.info('OK - copyBtn - present: ' + shouldBePresent);
                    }, function(val) {
                        throw ('NOK - copyBtn - expected isPresent to be:' + shouldBePresent);
                    }));
            } else if ('ersätta' === button) {
                promiseArr.push(expect(element(by.id('ersattBtn')).isPresent()).to.become(shouldBePresent)
                    .then(function(val) {
                        logger.info('OK - ersattBtn - present: ' + shouldBePresent);
                    }, function(val) {
                        throw ('NOK - ersattBtn - expected isPresent to be:' + shouldBePresent);
                    }));
            } else if ('förnya' === button) {
                promiseArr.push(expect(element(by.id('fornyaBtn')).isPresent()).to.become(shouldBePresent)
                    .then(function(val) {
                        logger.info('OK - fornyaBtn - present: ' + shouldBePresent);
                    }, function(val) {
                        throw ('NOK - fornyaBtn - expected isPresent to be:' + shouldBePresent);
                    }));
            } else if ('makulera' === button) {
                promiseArr.push(expect(element(by.id('makuleraBtn')).isPresent()).to.become(shouldBePresent)
                    .then(function(val) {
                        logger.info('OK - makuleraBtn - present: ' + shouldBePresent);
                    }, function(val) {
                        throw ('NOK - makuleraBtn - expected isPresent to be:' + shouldBePresent);
                    }));
            } else {
                throw ('Felaktig check. Hantering av knapp: ' + button + ' finns inte');
            }

        });
        return Promise.all(promiseArr);
    });

};
