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

/* global wcTestTools, pages, protractor, intyg, logger, ursprungligtIntyg */

'use strict';
var soap = require('soap');
var soapMessageBodies = require('./soap');
var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var db = require('./dbActions');
var tsBasintygtPage = pages.intyg.ts.bas.intyg;
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('./helpers');

function assertDraftWithStatus(personId, intygsId, status, cb) {
    setTimeout(function() {
        var databaseTable = process.env.DATABASE_NAME + '.INTYG';
        var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
            databaseTable + '.PATIENT_PERSONNUMMER="' + personId + '" AND ' +
            databaseTable + '.STATUS="' + status + '" AND ' +
            databaseTable + '.INTYGS_ID="' + intygsId + '" ;';

        assertNumberOfEvents(query, 1, cb);
    }, 5000);
}

function assertDatabaseContents(intygsId, column, value, cb) {
    setTimeout(function() {
        var databaseTable = process.env.DATABASE_NAME + '.INTYG';
        var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
            databaseTable + '.INTYGS_ID="' + intygsId + '" AND ' +
            databaseTable + '.' + column + '="' + value + '";';

        assertNumberOfEvents(query, 1, cb);
    }, 5000);
}

function assertEvents(intygsId, event, numEvents, cb) {
    setTimeout(function() {
        var databaseTable = 'webcert_requests.requests';
        var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
            databaseTable + '.handelseKod = "' + event + '" AND ' +
            databaseTable + '.utlatandeExtension="' + intygsId + '" ;';

        assertNumberOfEvents(query, numEvents, cb);
    }, 5000);
}

function assertNumberOfEvents(query, numEvents, cb) {
    // logger.debug('Assert number of events. Query: ' + query);
    var conn = db.makeConnection();
    conn.connect();
    conn.query(query,
        function(err, rows, fields) {
            conn.end();
            if (err) {
                cb(err);
            } else if (rows[0].Counter !== numEvents) {
                cb('FEL, Antal händelser i db: ' + rows[0].Counter + ' (' + numEvents + ')');
            } else {
                logger.info('OK - Antal händelser i db ' + rows[0].Counter + '(' + numEvents + ')');
                cb();
            }
        });
}

module.exports = function() {

    this.Then(/^ska intygsutkastets status vara "([^"]*)"$/, function(statustext, callback) {
        expect(tsBasintygtPage.intygStatus.getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Given(/^jag fyller i fältet "([^"]*)"$/, function(arg1, callback) {

        if (arg1 === 'Min undersökning av patienten') {
            fk7263Utkast.minUndersokning.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    fk7263Utkast.diagnosKod.sendKeys('A00').then(callback);
                });
        } else if (arg1 === 'Arbetsförmåga') {
            fk7263Utkast.faktiskTjanstgoring.sendKeys('40')
                .then(function() {
                    fk7263Utkast.nedsattMed25Checkbox.sendKeys(protractor.Key.SPACE).then(callback);
                });
        } else {
            callback();
        }
    });

    this.Given(/^jag fyller i resten av de nödvändiga fälten\.$/, function(callback) {
        fk7263Utkast.funktionsNedsattning.sendKeys('Halt och lytt').then(function() {
            fk7263Utkast.aktivitetsBegransning.sendKeys('Orkar inget').then(function() {
                fk7263Utkast.nuvarandeArbete.sendKeys('Stuveriarbetare').then(callback);
            });
        });
    });

    this.Given(/^ska statusuppdatering "([^"]*)" skickas till vårdsystemet\. Totalt: "([^"]*)"$/, function(arg1, arg2, callback) {
        assertEvents(global.intyg.id, arg1, parseInt(arg2, 10), callback);
    });

    this.Given(/^ska (\d+) statusuppdatering "([^"]*)" skickas för det ursprungliga intyget$/, function(antal, handelse, callback) {
        assertEvents(ursprungligtIntyg.id, handelse, parseInt(antal, 10), callback);
    });

    this.Given(/^är intygets status "([^"]*)"$/, function(arg1, callback) {
        assertDraftWithStatus(global.person.id, global.intyg.id, arg1, callback);
    });

    this.Given(/^är innehåller databasfältet "([^"]*)" värdet "([^"]*)"$/, function(arg1, arg2, callback) {
        assertDatabaseContents(global.intyg.id, arg1, arg2, callback);
    });

    this.Given(/^jag raderar intyget$/, function(callback) {
        fk7263Utkast.radera.knapp.sendKeys(protractor.Key.SPACE).then(function() {
            fk7263Utkast.radera.bekrafta.sendKeys(protractor.Key.SPACE).then(callback);
        });
    });

    this.Given(/^Försäkringskassan (?:har ställt|ställer) en "([^"]*)" fråga om intyget$/, function(amne, callback) {
        var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-question/v1.0?wsdl';
        url = url.replace('https', 'http');

        // global.person.id = '19121212-1212';

        //Kontrollnr for tester. Anvand i bade fraga och svar
        global.intyg.guidcheck = testdataHelper.generateTestGuid();

        var body = soapMessageBodies.ReceiveMedicalCertificateQuestion(
            global.person.id,
            global.user,
            'Enhetsnamn',
            global.intyg.id,
            amne,
            'nytt meddelande: ' + global.intyg.guidcheck);
        soap.createClient(url, function(err, client) {
            if (err) {
                callback(err);
            }

            client.ReceiveMedicalCertificateQuestion(body, function(err, result, body) {
                // logger.debug(body);
                // logger.debug(result);
                callback(err);
            });
        });
    });

    this.Given(/^Försäkringskassan skickar ett svar$/, function(callback) {

        var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + '/services/receive-answer/v1.0?wsdl';
        url = url.replace('https', 'http');

        soap.createClient(url, function(err, client) {

            if (err) {
                callback(err);
            } else {
                var body = soapMessageBodies.ReceiveMedicalCertificateAnswer(
                    global.person.id,
                    global.user.hsaId,
                    global.user.fornamn + '' + global.user.efternamn,
                    global.user.enhetId,
                    'WebCert Enhet 1',
                    'Enhetsnamn',
                    intyg.fragaId
                );
                console.log(body);
                client.ReceiveMedicalCertificateAnswer(body, function(err, result, body) {
                    callback(err);
                });
            }

        });
    });
};
