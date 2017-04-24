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

/* globals intyg, browser, logger, protractor, wcTestTools, Promise */
'use strict';
var db = require('./dbActions');
var request = require('request');
var loginHelperStatistik = require('./inloggning/login.helpers.statistik.js');
var logInAsUserRoleStatistik = loginHelperStatistik.logInAsUserRoleStatistik;
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;

var restAPIOptions = {
    url: 'https://statistik.ip30.nordicmedtest.sjunet.org/api/testsupport/processIntyg',
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength('')
    },
    body: ''
};

global.statistik = {
    diagnosKod: 'Z76',
    nrOfSjukfall: 0,
    intygsId: ''
};

module.exports = function() {

    this.Given(/^ska jag se intyget i databasen$/, function(callback) {
        db.statistics.lookUp(1, intyg.id, callback);
    });

    this.Given(/^radera de intyg som har diagnoskod "([^"]*)" från wideline tabellen i statitikdatabasen$/, function(diagnosKod, callback) {
        db.statistics.deleteSjukfall(diagnosKod, callback);
    });

    this.Given(/^jag går in på Statistiktjänsten$/, function() {
        global.statistik.intygsId = intyg.id;
        var url = process.env.STATISTIKTJANST_URL + '/#/fakelogin';
        return browser.get(url).then(function() {
            logger.info('Går till url: ' + url);
        });
    });

    this.Given(/^jag är inloggad som läkare i Statistiktjänsten$/, function() {
        // Setting rehabstod to new bas url
        browser.baseUrl = process.env.STATISTIKTJANST_URL;
        // VG_TestAutomation => TSTNMT2321000156-107M => TSTNMT2321000156-107Q
        var userObj = {
            forNamn: 'Johan',
            efterNamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            vardgivarIdSomProcessLedare: [
                'TSTNMT2321000156-107M'
            ],
            vardgivarniva: 'true'
        };

        return logInAsUserRoleStatistik(userObj, 'Läkare', true);
    });

    this.Given(/^jag ändrar diagnoskoden till "([^"]*)"$/, function(diagnoskod) {
        element(by.id('smittskydd')).isSelected().then(function(isSelected) {
            if (isSelected) {
                return element(by.id('smittskydd')).sendKeys(protractor.Key.SPACE).then(function() {
                    element(by.id('basedOnExamination')).isPresent().then(function(isPresent) {
                        if (isPresent) {
                            return element(by.id('basedOnExamination')).sendKeys(protractor.Key.SPACE).then(function() {
                                return element(by.id('disabilities')).sendKeys('disabilities').then(function() {
                                    return element(by.id('activityLimitation')).sendKeys('activityLimitation').then(function() {
                                        return element(by.id('currentWork')).sendKeys('currentWork').then(function() {
                                            return fkUtkastPage.diagnosKod.clear().then(function() {
                                                return fkUtkastPage.angeDiagnosKod(diagnoskod);
                                            });
                                        });
                                    });
                                });
                            });
                        }
                    });
                });
            } else {
                logger.info('Inget smittaintyg, ändra enbart diagnoskod till %s', diagnoskod);
                return fkUtkastPage.diagnosKod.clear().then(function() {
                    return fkUtkastPage.angeDiagnosKod(diagnoskod);
                });
            }
        });
    });

    this.Given(/^jag går till statistiksidan för diagnoskod Z76$/, function() {
        // 736870bed816a6a18a65b01c05dc3e44 === diagnoskod 'Z76'
        var url = process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/736870bed816a6a18a65b01c05dc3e44?vgid=TSTNMT2321000156-107M';
        return browser.get(url).then(function() {
            logger.info('Går till url för diagnoskod Z76: ' + url);
        });
    });

    this.Given(/^(?:ska|jag kollar att) totala "([^"]*)" diagnoser som finns (?:vara|är) "([^"]*)"$/, function(diagnosKod, nrOfIntygs) {
        if (diagnosKod && nrOfIntygs) {
            global.statistik.diagnosKod = diagnosKod;
            nrOfIntygs = parseInt(nrOfIntygs, 10);
            if (global.statistik.nrOfSjukfall === 1 && nrOfIntygs === 0) {
                global.statistik.nrOfSjukfall = global.statistik.nrOfSjukfall - 1;
            } else {
                global.statistik.nrOfSjukfall += nrOfIntygs;
            }
        } else {
            return Promise.reject('diagnosKod och nrOfIntygs får inte vara tomma.');
        }

        return element.all(by.css('.table-condensed')).then(function(promiseArr) {
            return promiseArr.forEach(function(entry) {
                return entry.getText().then(function(txt) {
                    if (txt.indexOf(diagnosKod) === -1) {
                        if (txt.startsWith(nrOfIntygs)) { // => Antal sjukfall totalt
                            logger.info('Antal intyg i GUI: %s', nrOfIntygs);
                            return expect(global.statistik.nrOfSjukfall).to.equal(nrOfIntygs);
                        } else {
                            logger.info('Antal intyg i GUI: %s', txt.split(' ')[0]);
                            return Promise.reject();
                        }
                    }
                });
            });
        });
    });

    this.Given(/^jag anropar statitisk-APIet processIntyg$/, function() {
        return browser.sleep(10000).then(function() {
            var defer = protractor.promise.defer();

            request(restAPIOptions, function(error, message) {
                if (error || message.statusCode >= 400) {
                    logger.info('Request error:', error);
                    if (message) {
                        logger.info('Error message:', message.statusCode, message.statusMessage /*, body*/ );
                    }
                    defer.fulfill({
                        error: error,
                        message: message
                    });
                } else {
                    logger.info('Request success!', message.statusCode, message.statusMessage);
                    defer.fulfill(message);
                }
            });
            return defer.promise;
        });
    });

};
