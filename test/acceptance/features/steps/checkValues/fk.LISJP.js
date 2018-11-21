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

/* globals logger, pages, Promise, wcTestTools */

'use strict';

var testdataHelper = wcTestTools.helpers.testdata;
var lisjpPage = pages.intyg.lisjp.intyg;

function checkBaseratPa(baseratPa) {
    var minUndersokningText = testdataHelper.ejAngivetIfNull(baseratPa.minUndersokningAvPatienten);
    var journaluppgifterText = testdataHelper.ejAngivetIfNull(baseratPa.journaluppgifter);
    var annatText = testdataHelper.ejAngivetIfNull(baseratPa.annat);
    var annatBeskrivningText = testdataHelper.ejAngivetIfNull(baseratPa.annatBeskrivning);

    return Promise.all([
        expect(lisjpPage.baseratPa.minUndersokningAvPatienten.getText()).to.eventually.equal(minUndersokningText),
        expect(lisjpPage.baseratPa.journaluppgifter.getText()).to.eventually.equal(journaluppgifterText),
        expect(lisjpPage.baseratPa.annat.getText()).to.eventually.equal(annatText),
        expect(lisjpPage.baseratPa.annatBeskrivning.getText()).to.eventually.equal(annatBeskrivningText)
    ]);
}

function checkDiagnos(diagnos) {
    diagnos.diagnoser = [];
    diagnos.diagnoser.push({
        kod: diagnos.kod,
        bakgrund: diagnos.bakgrund
    });
    var diagnoser = diagnos.diagnoser;
    var promiseArr = [];
    for (var i = 0; i < diagnoser.length; i++) {
        promiseArr.push(expect(lisjpPage.diagnoser.getDiagnos(i).kod.getText()).to.eventually.equal(diagnoser[i].kod));
    }

    return Promise.all(promiseArr);

}

function checkFunktionsnedsattning(nedsattning) {
    return expect(lisjpPage.funktionsnedsattning.getText()).to.eventually.equal(nedsattning);
}

function checkAktivitetsbegransning(begr) {
    return expect(lisjpPage.aktivitetsbegransning.getText()).to.eventually.equal(begr);
}

module.exports = {
    checkValues: function(intyg, callback) {
        logger.info('-- Kontrollerar Läkarintyg för sjukpenning --');
        logger.warn('intyg med typ: ' + intyg.typ + ' saknar funktioner för kontroll av data');
        return Promise.all([ //kommer snart
            //Baserat på
            checkBaseratPa(intyg.baseratPa)
            .then(function(value) {
                logger.info('OK - Baseras på');
            }, function(reason) {
                throw ('FEL, Baseras på: ' + reason);
            }),

            //Diagnoser
            checkDiagnos(intyg.diagnos)
            .then(function(value) {
                logger.info('OK - Diagnos');
            }, function(reason) {
                throw ('FEL, Diagnos: ' + reason);
            }),

            //Funktionsnedsättning
            checkFunktionsnedsattning(intyg.funktionsnedsattning)
            .then(function(value) {
                logger.info('OK - Funktionsnedsättning');
            }, function(reason) {
                throw ('FEL, Funktionsnedsättning: ' + reason);
            }),

            //Aktivitestbegränsning
            checkAktivitetsbegransning(intyg.aktivitetsbegransning)
            .then(function(value) {
                logger.info('OK - Aktivitestbegränsning');
            }, function(reason) {
                throw ('FEL, Aktivitestbegränsning: ' + reason);
            })
        ]);
    }
};
