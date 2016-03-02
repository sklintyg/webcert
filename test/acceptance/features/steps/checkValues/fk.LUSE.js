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

/* globals logger */

'use strict';

// var helpers = require('./helpers.js');
// var lusePage = pages.intyg.luse.intyg;

// function checkUtlatandeDatum(utlatandeText, cb) {
//     if (utlatandeText !== 'Annat underlag för utlåtandet') {
//         expect(element(by.cssContainingText('.intyg-field', utlatandeText)).getText()).to.eventually.contain(helpers.getDateForAssertion()).then(function(date) {
//             logger.info('OK - ' + utlatandeText + '  = ' + date);
//         }, function(reason) {
//             cb('FEL, Min undersökning av patienten,' + reason);
//         });
//     } else {
//         expect(element(by.cssContainingText('.intyg-field', utlatandeText)).getText()).to.eventually.contain(utlatandeText).then(function(date) {
//             logger.info('OK - ' + utlatandeText + '  = ' + date);
//         }, function(reason) {
//             cb('FEL, Min undersökning av patienten,' + reason);
//         });
//     }
// }

// function checkAndraMedUtrUnd(underlagFinnsNo, cb) {
//     // får för tilfället ett nej oavsett.
//     expect(element(by.id('underlagFinnsNo')).getText()).to.eventually.equal('Nej').then(function() {
//         logger.info('OK - ' + underlagFinnsNo);
//     }, function(reason) {
//         cb('FEL, Finns det andra medicinska utredningar eller underlag som är relevanta för bedömningen?,' + reason);
//     });
// }

// function checkSjukForLopp(sjukdomsforlopp, cb) {

//     expect(element(by.cssContainingText('.ng-binding.ng-scope', sjukdomsforlopp)).getText()).to.eventually.equal(sjukdomsforlopp).then(function() {
//         logger.info('OK - ' + sjukdomsforlopp);
//     }, function(reason) {
//         cb('FEL, Sjukdomsförlopp för aktuella sjukdomar av betydelse' + reason);
//     });
// }

// function checkDiagnosNedArbFor(kod, cb) {
//     expect(lusePage.diagnoseCode.getText()).to.eventually.equal(kod).then(function() {
//         logger.info('OK - ' + kod);
//     }, function(reason) {
//         cb('FEL, Diagnoskod enligt ICD-10 SE' + reason);
//     });
// }
module.exports = {
    checkValues: function(intyg, callback) {
        logger.info('-- Kontrollerar Läkarutlåtande för sjukersättning --');
        logger.warn('intyg med typ: ' + intyg.typ + 'skapa kontroll av data EJ KLAR!!');
        callback();
        // checkUtlatandeDatum('Min undersökning av patienten.', callback);
        // checkUtlatandeDatum('Journaluppgifter från den', callback);
        // checkUtlatandeDatum('Anhörigs beskrivning av patienten', callback);
        // checkUtlatandeDatum('Annat', callback);
        // checkUtlatandeDatum('Annat underlag för utlåtandet', callback);
        // checkUtlatandeDatum('Jag har känt patienten seden den', callback);

        // checkAndraMedUtrUnd('Nej', callback);

        // checkSjukForLopp(intyg.sjukdomsForlopp, callback);
        // checkDiagnosNedArbFor(intyg.diagnos.kod, callback);

        // expect(element(by.id('underlagFinnsNo')).getText()).to.eventually.equal('Nej').then(function() {
        //     logger.info('OK - Nej');
        // logger.info('OK - ' + underlagFinnsNo);
        // }, function(reason) {
        //     callback('FEL, Finns det andra medicinska utredningar eller underlag som är relevanta för bedömningen?,' + reason);
        // }).then(callback);
    }
};
