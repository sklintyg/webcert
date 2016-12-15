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

/* globals logger, pages, JSON, browser, Promise */

'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
module.exports = {
    fillIn: function(intyg) {
        browser.ignoreSynchronization = true;
        var promisesArr = [];

        //Ange smittskydd
        promisesArr.push(fkUtkastPage.angeSmittskydd(intyg.smittskydd).then(function() {
            logger.info('OK - angeSmittskydd :' + intyg.smittskydd);
        }, function(reason) {
            throw ('FEL, angeSmittskydd,' + reason);
        }));

        //Ange baseras på
        promisesArr.push(fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa).then(function() {
            logger.info('OK - angeIntygetBaserasPa :' + JSON.stringify(intyg.baserasPa));
        }, function(reason) {
            throw ('FEL, angeIntygetBaserasPa,' + reason);
        }));

        //Ange funktionsnedsättning
        promisesArr.push(fkUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning).then(function() {
            logger.info('OK - angeFunktionsnedsattning :' + JSON.stringify(intyg.funktionsnedsattning));
        }, function(reason) {
            throw ('FEL, angeFunktionsnedsattning,' + reason);
        }));

        browser.ignoreSynchronization = false;
        //Ange diagnoser
        promisesArr.push(fkUtkastPage.angeDiagnoser(intyg.diagnos).then(function() {
            logger.info('OK - angeDiagnoser :' + JSON.stringify(intyg.diagnos));
        }, function(reason) {
            throw ('FEL, angeDiagnoser,' + reason);
        }));

        //Ange aktuellt sjukdomsförlopp
        promisesArr.push(fkUtkastPage.angeAktuelltSjukdomsForlopp(intyg.aktuelltSjukdomsforlopp).then(function() {
            logger.info('OK - angeAktuelltSjukdomsForlopp :' + JSON.stringify(intyg.aktuelltSjukdomsforlopp));
        }, function(reason) {
            throw ('FEL, angeAktuelltSjukdomsForlopp,' + reason);
        }));

        //Ange aktivitetsbegränsning
        promisesArr.push(fkUtkastPage.angeAktivitetsBegransning(intyg.aktivitetsBegransning).then(function() {
            logger.info('OK - angeAktivitetsBegransning :' + JSON.stringify(intyg.aktivitetsBegransning));
        }, function(reason) {
            throw ('FEL, angeAktivitetsBegransning,' + reason);
        }));

        promisesArr.push(fkUtkastPage.angeArbete(intyg.arbete).then(function() {
            logger.info('OK - angeArbete :' + JSON.stringify(intyg.arbete));
        }, function(reason) {
            throw ('FEL, angeArbete,' + reason);
        }));
        promisesArr.push(fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function() {
            logger.info('OK - angeArbetsformaga :' + JSON.stringify(intyg.arbetsformaga));
        }, function(reason) {
            throw ('FEL, angeArbetsformaga,' + reason);
        }));
        promisesArr.push(fkUtkastPage.angeArbetsformagaFMB(intyg.arbetsformagaFMB).then(function() {
            logger.info('OK - angeArbetsformagaFMB :' + JSON.stringify(intyg.arbetsformagaFMB));
        }, function(reason) {
            throw ('FEL, angeArbetsformagaFMB,' + reason);
        }));

        promisesArr.push(fkUtkastPage.angePrognos(intyg.prognos).then(function() {
            logger.info('OK - angePrognos :' + JSON.stringify(intyg.prognos));
        }, function(reason) {
            throw ('FEL, angePrognos, value:' +
                JSON.stringify(intyg.prognos) + ' ,' + reason);
        }));
        browser.ignoreSynchronization = true;

        promisesArr.push(fkUtkastPage.angeKontaktOnskasMedFK(intyg.kontaktOnskasMedFK).then(function() {
            logger.info('OK - angeKontaktOnskasMedFK :' + JSON.stringify(intyg.kontaktOnskasMedFK));
        }, function(reason) {
            throw ('FEL, angeKontaktOnskasMedFK,' + reason);
        }));

        promisesArr.push(fkUtkastPage.angeRekommendationer(intyg.rekommendationer).then(function() {
            logger.info('OK - angeRekommendationer :' + JSON.stringify(intyg.rekommendationer));
        }, function(reason) {
            throw ('FEL, angeRekommendationer,' + reason);
        }));

        // //Ange enhetadress
        // promisesArr.push(fkUtkastPage.angeEnhetAdress(global.user.enhetsAdress).then(function() {
        //     logger.info('OK - angeEnhetAdress :' + JSON.stringify(global.user.enhetsAdress));
        // }, function(reason) {
        //     throw ('FEL, angeEnhetAdress,' + reason);
        // }));

        return Promise.all(promisesArr)
            .then(function(value) {
                browser.ignoreSynchronization = false;
            });
    }
};
