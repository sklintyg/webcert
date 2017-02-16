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

/* globals pages, browser, Promise, logger, JSON */

'use strict';
var luaefsUtkastPage = pages.intyg.luaeFS.utkast;
module.exports = {
    fillIn: function(intyg) {
        logger.info('intyg.typ:' + intyg.typ);
        var promiseArr = [];
        console.log(intyg);

        //Baserat på
        promiseArr.push(luaefsUtkastPage.angeBaseratPa(intyg.baseratPa)
            .then(function(value) {
                logger.info('Baseras på: ' + JSON.stringify(intyg.baseratPa));
            }, function(reason) {
                return Promise.reject('FEL, Baseras på: ' + reason);
            })
        );


        //Andra medicinska utredningar
        promiseArr.push(luaefsUtkastPage.angeAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
            .then(function(value) {
                logger.info('Andra medicinska utredningar: ' + JSON.stringify(intyg.andraMedicinskaUtredningar));
            }, function(reason) {
                return Promise.reject('FEL, Andra medicinska utredningar: ' + reason);
            })
        );

        // //Sjukdomsförlopp // ANVÄNDA EJ I LUAE_FS
        // promiseArr.push(luaefsUtkastPage.angeSjukdomsforlopp(intyg.sjukdomsForlopp)
        //     .then(function(value) {
        //         logger.info('Sjukdomsförlopp: ' + JSON.stringify(intyg.sjukdomsForlopp));
        //     }, function(reason) {
        //         return Promise.reject('FEL, Sjukdomsförlopp: ' + reason);
        //     })
        // );

        //Diagnoser
        promiseArr.push(luaefsUtkastPage.angeDiagnos(intyg.diagnos)
            .then(function(value) {
                logger.info('Diagnos: ' + JSON.stringify(intyg.diagnos));
            }, function(reason) {
                return Promise.reject('FEL, Diagnos: ' + reason);
            })
        );

        //Funktionsnedsättning
        promiseArr.push(luaefsUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning)
            .then(function(value) {
                logger.info('Funktionsnedsättning: ' + JSON.stringify(intyg.funktionsnedsattning));
            }, function(reason) {
                return Promise.reject('FEL, Funktionsnedsättning: ' + reason);
            })
        );

        // //aktivitetsbegränsning
        // promiseArr.push(luaefsUtkastPage.angeAktivitetsbegransning(intyg.aktivitetsbegransning)
        //     .then(function(value) {
        //         logger.info('Aktivitetsbegränsning: ' + JSON.stringify(intyg.aktivitetsbegransning));
        //     }, function(reason) {
        //         return Promise.reject('FEL, Aktivitetsbegränsning: ' + reason);
        //     })
        // );
        //
        // //Medicinsk behandling
        // promiseArr.push(luaefsUtkastPage.angeMedicinskBehandling(intyg.medicinskbehandling)
        //     .then(function(value) {
        //         logger.info('Medicinsk behandling: ' + JSON.stringify(intyg.medicinskbehandling));
        //     }, function(reason) {
        //         return Promise.reject('FEL, Medicinsk behandling: ' + reason);
        //     })
        // );
        //
        // //Medicinska förutsättningar
        // promiseArr.push(luaefsUtkastPage.angeMedicinskaForutsattningar(intyg.medicinskaForutsattningar)
        //     .then(function(value) {
        //         logger.info('Medicinska förutsättningar: ' + JSON.stringify(intyg.medicinskaForutsattningar));
        //     }, function(reason) {
        //         return Promise.reject('FEL, Medicinska förutsättningar: ' + reason);
        //     })
        // );
        //
        //Övriga upplysningar
        promiseArr.push(luaefsUtkastPage.angeOvrigaUpplysningar(intyg.ovrigt)
            .then(function(value) {
                logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.ovrigt));
            }, function(reason) {
                return Promise.reject('FEL, Övriga upplysningar: ' + reason);
            })
        );

        //Kontakt med FK
        promiseArr.push(luaefsUtkastPage.angeKontaktMedFK(intyg.kontaktMedFk)
            .then(function(value) {
                logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.kontaktMedFk));
            }, function(reason) {
                return Promise.reject('FEL, Övriga upplysningar: ' + reason);
            })
        );
        browser.sleep(30000);
        return Promise.all(promiseArr).then(function(value) {
            browser.ignoreSynchronization = false;
        }, function(reason) {
            throw (reason);
        });
    }
};
