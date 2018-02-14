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

/* global pages, protractor, ursprungligtIntyg, Promise, JSON, intyg, logger */

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');


var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var db = require('./dbActions');
var tsBasintygtPage = pages.intyg.ts.bas.intyg;
var statusuppdateringarRows;
var helpers = require('./helpers');

/*
 *	Stödfunktioner
 *
 */


function getNotificationEntries(intygsId, value, numEvents) {
    var isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
    var isTSIntyg = helpers.isTSIntyg(intyg.typ);
    var table = 'webcert_requests.requests';
    var handelseTidName = 'handelsetidpunkt';
    var extensionType = 'utlatandeExtension';
    var selectStatement = 'SELECT utlatandeExtension, handelseKod,antalFragor,antalHanteradeFragor,antalSvar,antalHanteradeSvar, ' + handelseTidName;

    if (isSMIIntyg || isTSIntyg) {
        table = 'webcert_requests.statusupdates_3';
        handelseTidName = 'handelseTid';
        extensionType = 'intygsExtension';

        selectStatement = 'SELECT ' + extensionType + ', handelseKod,' +
            ' skickadeFragorTotal, skickadeFragorEjBesvarade, skickadeFragorBesvarade, skickadeFragorHanterade,' +
            ' mottagnaFragorTotal, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade, mottagnaFragorHanterade,' +
            handelseTidName + ', intygRef';

    }

    var databaseTable = table;
    var query = selectStatement + ' FROM ' + databaseTable +
        ' WHERE ' + databaseTable + '.handelseKod = "' + value + '"' +
        ' AND ' + databaseTable + '.' + extensionType + ' = "' + intygsId + '"' +
        ' ORDER BY ' + handelseTidName + ' DESC;';

    console.log('query: ' + query);


    var promise = new Promise(function(resolve, reject) {

        db.smPool.getConnection().then(function(connection) {
            connection.query(query,
                function(err, rows, fields) {
                    connection.release();
                    logger.info('MySQL Connection is released back into the pool');
                    if (err) {
                        reject(err);
                        // } else if (rows.length !== numEvents) {
                        //     // console.log('FEL, Antal händelser i db: ' + rows[0].Counter + ' (' + numEvents + ')');
                        //     resolve();
                    } else {
                        console.log('Antal händelser i db ' + rows.length + '(' + numEvents + ')');
                        resolve(rows);
                    }
                });
        });


    });
    return promise;
}

function waitForEntries(intygsId, statusValue, numEvents, cb) {
    var intervall = 5000;

    getNotificationEntries(intygsId, statusValue, numEvents).then(function(result) {
        if (result.length >= numEvents) {
            console.log('Hittade rader: ' + JSON.stringify(result));
            statusuppdateringarRows = result;
            cb();
        } else {
            console.log('Hittade färre än ' + numEvents + 'rader i databasen');
            console.log('Ny kontroll sker efter ' + intervall + 'ms');
            setTimeout(function() {
                waitForEntries(intygsId, statusValue, numEvents, cb);
            }, intervall);
        }

    }, function(err) {
        cb(err);

    });
}

/*
 *	Test steg
 *
 */

Then(/^ska intygsutkastets status vara "([^"]*)"$/, function(statustext, callback) {
    expect(tsBasintygtPage.intygStatus.getText()).to.eventually.contain(statustext).and.notify(callback);
});

Given(/^ska statusuppdatering "([^"]*)" skickas till vårdsystemet\. Totalt: "([^"]*)"$/, function(handelsekod, antal, callback) {
    waitForEntries(global.intyg.id, handelsekod, parseInt(antal, 10), callback);
});

Given(/^ska (\d+) statusuppdatering "([^"]*)" skickas för det ursprungliga intyget$/, function(antal, handelsekod, callback) {
    waitForEntries(ursprungligtIntyg.id, handelsekod, parseInt(antal, 10), callback);
});

Given(/^jag raderar intyget$/, function(callback) {
    fk7263Utkast.radera.knapp.sendKeys(protractor.Key.SPACE).then(function() {
        fk7263Utkast.radera.bekrafta.sendKeys(protractor.Key.SPACE).then(callback);
    });
});

Given(/^ska statusuppdateringen visa att parametern "([^"]*)" är mottagen med värdet "([^"]*)"$/, function(param, paramValue) {
    console.log(statusuppdateringarRows[0]);
    var row = statusuppdateringarRows[0];
    var dbParam = (param === 'ref') ? 'intygRef' : 'undefined';
    console.log('\'' + param + '\' converted to database equivalent => \'' + dbParam + '\'');
    expect(paramValue).to.equal(row[dbParam].toString());
});

Given(/^ska statusuppdateringen visa frågor (\d+), hanterade frågor (\d+),antal svar (\d+), hanterade svar (\d+)$/, function(fragor, hanFragor, svar, hanSvar) {
    logger.info(statusuppdateringarRows[0]);
    var row = statusuppdateringarRows[0];
    return Promise.all([
        expect(fragor).to.equal(row.antalFragor.toString()),
        expect(hanFragor).to.equal(row.antalHanteradeFragor.toString()),
        expect(svar).to.equal(row.antalSvar.toString()),
        expect(hanSvar).to.equal(row.antalHanteradeSvar.toString())
    ]);
});


Given(/^ska statusuppdateringen visa mottagna frågor totalt (\d+),ej besvarade (\d+),besvarade (\d+), hanterade (\d+)$/,
    function(totalt, ejBesvarade, besvarade, hanterade) {
        logger.info(statusuppdateringarRows[0]);
        var row = statusuppdateringarRows[0];

        return Promise.all([
            expect(totalt).to.equal(row.mottagnaFragorTotal.toString()),
            expect(ejBesvarade).to.equal(row.mottagnaFragorEjBesvarade.toString()),
            expect(besvarade).to.equal(row.mottagnaFragorBesvarade.toString()),
            expect(hanterade).to.equal(row.mottagnaFragorHanterade.toString())
        ]);
    }
);

Given(/^ska statusuppdateringen visa skickade frågor totalt (\d+),ej besvarade (\d+),besvarade (\d+), hanterade (\d+)$/,
    function(totalt, ejBesvarade, besvarade, hanterade) {
        logger.info(statusuppdateringarRows[0]);
        var row = statusuppdateringarRows[0];

        return Promise.all([
            expect(totalt).to.equal(row.skickadeFragorTotal.toString()),
            expect(ejBesvarade).to.equal(row.skickadeFragorEjBesvarade.toString()),
            expect(besvarade).to.equal(row.skickadeFragorBesvarade.toString()),
            expect(hanterade).to.equal(row.skickadeFragorHanterade.toString())
        ]);
    }
);
