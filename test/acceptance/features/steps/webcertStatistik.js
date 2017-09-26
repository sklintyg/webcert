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
var diagnosKategorier = wcTestTools.testdata.diagnosKategorier;
var shuffle = wcTestTools.helpers.testdata.shuffle;

var restAPIOptions = {
    url: process.env.WEBCERT_URL + '/api/testsupport/processIntyg',
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength('')
    },
    body: ''
};

global.statistik = {
    diagnosKod: false,
    nrOfSjukfall: {
        totalt: 0,
        kvinna: 0,
        man: 0
    },
    intygsId: ''
};

function slumpaDiagnosKod(diagnosKod) {

    if (diagnosKod === 'slumpad') {
        diagnosKod = shuffle(diagnosKategorier)[0].diagnosKod;
        global.statistik.diagnosKod = diagnosKod;
        logger.info('==== Slumpat fram diagnosKod ' + diagnosKod + '====');
    } else if (diagnosKod === 'samma som ovan') {
        diagnosKod = global.statistik.diagnosKod;
        logger.info('==== Använder diagnosKod ' + diagnosKod + '====');
    }
    return diagnosKod;

}



module.exports = function() {

    this.Given(/^ska jag se intyget i databasen$/, function(callback) {
        db.statistics.lookUp(1, intyg.id, callback);
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
            fornamn: 'Johan',
            efternamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            vardgivarIdSomProcessLedare: [
                'TSTNMT2321000156-107M'
            ],
            vardgivarniva: 'true'
        };

        return logInAsUserRoleStatistik(userObj, 'Läkare', true);
    });

    this.Given(/^jag ändrar diagnoskoden till "([^"]*)"$/, function(diagnosKod) {
        diagnosKod = slumpaDiagnosKod(diagnosKod);



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
                                                return fkUtkastPage.angeDiagnosKod(diagnosKod);
                                            });
                                        });
                                    });
                                });
                            });
                        }
                    });
                });
            } else {
                logger.info('Inget smittaintyg, ändra enbart diagnoskod till %s', diagnosKod);
                return fkUtkastPage.diagnosKod.clear().then(function() {
                    return fkUtkastPage.angeDiagnosKod(diagnosKod);
                });
            }
        });
    });

    this.Given(/^jag går till statistiksidan för diagnoskod "([^"]*)"$/, function(diagnosKod) {
        diagnosKod = slumpaDiagnosKod(diagnosKod);


        // Alla kategorier (för många kategorier)
        //var url = process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/f07485ae393db737bacab5f416a43a2a?vgid=TSTNMT2321000156-107M';



        var url = {
            A: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/2d4fd1c2a5b880350fc1c606970cc51e?vgid=TSTNMT2321000156-107M',
            B: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/73afd9510058d0b8f9a18e7e3a1b0b78?vgid=TSTNMT2321000156-107M',
            C: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/493616c14076d5a2e7c7b3d34e357b2b?vgid=TSTNMT2321000156-107M',
            D: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/d9999cb555e54f7e4bb4991e38c5eb3b?vgid=TSTNMT2321000156-107M',
            E: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/583abd26f6fc34d556d8743c540c74ee?vgid=TSTNMT2321000156-107M',
            F: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/768a292b2f9bd2827f155b28a8f9443d?vgid=TSTNMT2321000156-107M'
        };

        logger.silly('diagnosKod.charAt(0) - ' + diagnosKod.charAt(0));
        logger.silly('url[diagnosKod.charAt(0)] - ' + url[diagnosKod.charAt(0)]);

        return browser.get(url[diagnosKod.charAt(0)]).then(function() {
            logger.info('Går till url för diagnoskod ' + diagnosKod + ': ' + url[diagnosKod.charAt(0)]);
        });
    });

    this.Given(/^jag kollar totala "([^"]*)" diagnoser som finns$/, function(diagnosKod) {
        diagnosKod = slumpaDiagnosKod(diagnosKod);


        return element.all(by.css('.table-condensed')).all(by.tagName('tr')).then(function(arr) {
            //return
            var statistik = [];
            logger.silly('Statistik Tabell Längd (arr.length): ' + arr.length);

            arr.forEach(function(entry, index) {

                entry.getText().then(function(txt) {

                    // Devide array in half and merge the data into statistik object based on index.
                    var secondIndex = index - arr.length / 2;

                    if (arr.length / 2 > index) {
                        statistik.push({
                            diagnosKod: txt
                        });
                    } else {
                        statistik[secondIndex].totalt = parseInt(txt.split(' ')[0], 10);
                        statistik[secondIndex].kvinna = parseInt(txt.split(' ')[1], 10);
                        statistik[secondIndex].man = parseInt(txt.split(' ')[2], 10);

                        logger.silly(statistik[secondIndex]);

                        if (statistik[secondIndex].diagnosKod.split(' ')[0] === diagnosKod) {
                            global.statistik.nrOfSjukfall = statistik[secondIndex];
                            logger.info('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);
                            return;
                        }
                    }
                });
                if (index === arr.length) {
                    throw ('Kunde inte hitta aktuellt antal intyg för ' + diagnosKod);
                }
            });
        });
    });


    this.Given(/^ska totala "([^"]*)" diagnoser som finns (?:vara|är) "([^"]*)" (extra|mindre)$/, function(diagnosKod, nrOfIntyg, modifer) {
        diagnosKod = slumpaDiagnosKod(diagnosKod);


        logger.silly(global.person);

        var gender = global.person.kon;

        logger.info('====== Kollar statistik på kön: ' + gender + '==========');

        if (!global.statistik.nrOfSjukfall) {
            throw ('test steget förväntar sig att tidigare steg kollat aktuell statistik.');
        }

        var nuvarandeStatistik = global.statistik.nrOfSjukfall;

        function raknaUtForvantatAntal(antal) {
            if (modifer === 'extra') {
                return antal + parseInt(nrOfIntyg, 10);
            } else if (modifer === 'mindre') {
                return antal - parseInt(nrOfIntyg, 10);
            } else {
                throw ('test steget förväntar sig extra eller mindre variabel.');
            }
        }

        if (!diagnosKod || !nrOfIntyg) {
            throw ('diagnosKod och nrOfIntyg får inte vara tomma.');
        } else {
            logger.silly('nuvarandeStatistik.totalt: ' + nuvarandeStatistik.totalt);
            nuvarandeStatistik.totalt = raknaUtForvantatAntal(nuvarandeStatistik.totalt);
            logger.silly('nuvarandeStatistik.totalt: ' + nuvarandeStatistik.totalt);

            if (gender === 'man') {
                nuvarandeStatistik.man = raknaUtForvantatAntal(nuvarandeStatistik.man);
            } else if (gender === 'kvinna') {
                nuvarandeStatistik.kvinna = raknaUtForvantatAntal(nuvarandeStatistik.kvinna);
            } else {
                throw ('Kunde inte fastställa kön på person: ' + global.person);
            }
        }
        global.statistik.nrOfSjukfall = nuvarandeStatistik;
        logger.silly('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);

        return element.all(by.css('.table-condensed')).all(by.tagName('tr')).then(function(arr) {
            //return
            var statistik = [];
            logger.info('Antal rader i tabellen: ' + arr.length);

            arr.forEach(function(entry, index) {

                entry.getText().then(function(txt) {

                    // Devide array in half and merge the data into statistik object based on index.
                    var secondIndex = index - arr.length / 2;

                    if (arr.length / 2 > index) {
                        statistik.push({
                            diagnosKod: txt
                        });
                    } else {
                        statistik[secondIndex].totalt = parseInt(txt.split(' ')[0], 10);
                        statistik[secondIndex].kvinna = parseInt(txt.split(' ')[1], 10);
                        statistik[secondIndex].man = parseInt(txt.split(' ')[2], 10);

                        logger.silly('Data på rad ' + secondIndex);
                        logger.silly(statistik[secondIndex]);

                        if (statistik[secondIndex].diagnosKod.split(' ')[0] === diagnosKod) {
                            logger.info('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);
                            logger.silly('statistik[secondIndex].totalt: ' + statistik[secondIndex].totalt);

                            logger.info('global.statistik.nrOfSjukfall.kvinna: ' + global.statistik.nrOfSjukfall.kvinna);
                            logger.silly('statistik[secondIndex].kvinna: ' + statistik[secondIndex].kvinna);

                            logger.info('global.statistik.nrOfSjukfall.man: ' + global.statistik.nrOfSjukfall.man);
                            logger.silly('statistik[secondIndex].man: ' + statistik[secondIndex].man);

                            var promiseArr = [];
                            promiseArr.push(expect(global.statistik.nrOfSjukfall.totalt).to.equal(statistik[secondIndex].totalt));
                            promiseArr.push(expect(global.statistik.nrOfSjukfall.kvinna).to.equal(statistik[secondIndex].kvinna));
                            promiseArr.push(expect(global.statistik.nrOfSjukfall.man).to.equal(statistik[secondIndex].man));

                            return Promise.all(promiseArr);

                        }
                    }
                });
                if (index === arr.length) {
                    throw ('Kunde inte hitta aktuellt antal intyg för ' + diagnosKod);
                }
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
