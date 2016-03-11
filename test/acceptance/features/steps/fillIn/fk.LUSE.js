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
var luseUtkastPage = pages.intyg.luse.utkast;
module.exports = {
    fillIn: function(intyg, callback) {
        logger.info('intyg.typ:' + intyg.typ);
        var promiseArr = [];
        console.log(intyg);
        browser.ignoreSynchronization = true;

        // //Baserat på
        promiseArr.push(luseUtkastPage.angeBaseratPa(intyg.baseratPa)
            .then(function(value) {
                logger.info('Baseras på: ' + JSON.stringify(intyg.baseratPa));
            }, function(reason) {
                return Promise.reject('FEL, Baseras på: ' + reason);
            })
        );

        //Andra medicinska utredningar
        promiseArr.push(luseUtkastPage.angeAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
            .then(function(value) {
                logger.info('Andra medicinska utredningar: ' + JSON.stringify(intyg.andraMedicinskaUtredningar));
            }, function(reason) {
                return Promise.reject('FEL, Andra medicinska utredningar: ' + reason);
            })
        );

        //Sjukdomsförlopp
        promiseArr.push(luseUtkastPage.angeSjukdomsforlopp(intyg.sjukdomsForlopp)
            .then(function(value) {
                logger.info('Sjukdomsförlopp: ' + JSON.stringify(intyg.sjukdomsForlopp));
            }, function(reason) {
                return Promise.reject('FEL, Sjukdomsförlopp: ' + reason);
            })
        );

        //Diagnoser
        promiseArr.push(luseUtkastPage.angeDiagnos(intyg.diagnos)
            .then(function(value) {
                logger.info('Diagnos: ' + JSON.stringify(intyg.diagnos));
            }, function(reason) {
                return Promise.reject('FEL, Diagnos: ' + reason);
            })
        );

        //Funktionsnedsättning
        promiseArr.push(luseUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning)
            .then(function(value) {
                logger.info('Funktionsnedsättning: ' + JSON.stringify(intyg.funktionsnedsattning));
            }, function(reason) {
                return Promise.reject('FEL, Funktionsnedsättning: ' + reason);
            })
        );

        //aktivitetsbegränsning
        promiseArr.push(luseUtkastPage.angeAktivitetsbegransning(intyg.aktivitetsbegransning)
            .then(function(value) {
                logger.info('Aktivitetsbegränsning: ' + JSON.stringify(intyg.aktivitetsbegransning));
            }, function(reason) {
                return Promise.reject('FEL, Aktivitetsbegränsning: ' + reason);
            })
        );

        //Medicinsk behandling
        promiseArr.push(luseUtkastPage.angeMedicinskBehandling(intyg.medicinskbehandling)
            .then(function(value) {
                logger.info('Medicinsk behandling: ' + JSON.stringify(intyg.medicinskbehandling));
            }, function(reason) {
                return Promise.reject('FEL, Medicinsk behandling: ' + reason);
            })
        );

        //Medicinska förutsättningar
        promiseArr.push(luseUtkastPage.angeMedicinskaForutsattningar(intyg.medicinskaForutsattningar)
            .then(function(value) {
                logger.info('Medicinska förutsättningar: ' + JSON.stringify(intyg.medicinskaForutsattningar));
            }, function(reason) {
                return Promise.reject('FEL, Medicinska förutsättningar: ' + reason);
            })
        );

        //Övriga upplysningar
        promiseArr.push(luseUtkastPage.angeOvrigaUpplysningar(intyg.ovrigt)
            .then(function(value) {
                logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.ovrigt));
            }, function(reason) {
                return Promise.reject('FEL, Övriga upplysningar: ' + reason);
            })
        );

        //Kontakt med FK
        promiseArr.push(luseUtkastPage.angeKontaktMedFK(intyg.kontaktMedFk)
            .then(function(value) {
                logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.kontaktMedFk));
            }, function(reason) {
                return Promise.reject('FEL, Övriga upplysningar: ' + reason);
            })
        );

        //Tilläggsfrågor
        promiseArr.push(luseUtkastPage.angeTillaggsfragor(intyg.tillaggsfragor)
            .then(function(value) {
                logger.info('Tilläggsfrågor: ' + JSON.stringify(intyg.tillaggsfragor));
            }, function(reason) {
                return Promise.reject('FEL, Tilläggsfrågor: ' + reason);
            })
        );

        Promise.all(promiseArr).then(function(value) {
            callback();
        }, function(reason) {
            callback(reason);
        });

        // luseUtkastPage.underlagFinnsNo.sendKeys(protractor.Key.SPACE);
        // // logger.info('Diagnoskod: ' + intyg.diagnos.kod);
        // luseUtkastPage.diagnoseCode.sendKeys(intyg.diagnos.kod);
        // luseUtkastPage.diagnoseCode.sendKeys(protractor.Key.TAB);
        // // logger.info('Diagnosbakgrund: ' + intyg.diagnos.bakgrund);
        // luseUtkastPage.diagnosgrund.sendKeys(intyg.diagnos.bakgrund);
        // luseUtkastPage.nyBedomningDiagnosgrundNo.sendKeys(protractor.Key.SPACE);

        // //Funktionsnedsättning
        // // logger.info('funktionsnedsattning: ' + intyg.funktionsnedsattning.intellektuell);
        // luseUtkastPage.funktionsnedsattning.intellektuell.sendKeys(intyg.funktionsnedsattning.intellektuell);
        // // logger.info('kommunikation: ' + intyg.funktionsnedsattning.KOMMUNIKATION);
        // luseUtkastPage.funktionsnedsattning.kommunikation.sendKeys(intyg.funktionsnedsattning.kommunikation);
        // // luseUtkastPage.funktionsnedsattning.koncentration.sendKeys(intyg.funktionsnedsattning.;
        // // logger.info('psykisk: ' + intyg.funktionsnedsattning.psykisk);
        // luseUtkastPage.funktionsnedsattning.psykisk.sendKeys(intyg.funktionsnedsattning.psykisk);
        // // logger.info('synHorselTal: ' + intyg.funktionsnedsattning.synHorselTal);
        // luseUtkastPage.funktionsnedsattning.synHorselTal.sendKeys(intyg.funktionsnedsattning.synHorselTal);
        // // logger.info('balansKoordination: ' + intyg.funktionsnedsattning.balansKoordination);
        // luseUtkastPage.funktionsnedsattning.balansKoordination.sendKeys(intyg.funktionsnedsattning.balansKoordination);
        // // logger.info('annan: ' + intyg.funktionsnedsattning.annan);
        // luseUtkastPage.funktionsnedsattning.annan.sendKeys(intyg.funktionsnedsattning.annan);
        // // logger.info('sjukdomsforlopp: ' + intyg.sjukdomsforlopp);
        // luseUtkastPage.sjukdomsforlopp.sendKeys(intyg.sjukdomsForlopp);
        // // logger.info('aktivitetsbegransning: ' + intyg.aktivitetsbegransning);
        // luseUtkastPage.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning);
        // // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        // luseUtkastPage.avslutadBehandling.sendKeys(intyg.avslutadBehandling);
        // // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        // luseUtkastPage.pagaendeBehandling.sendKeys(intyg.pagaendeBehandling);
        // // logger.info('planeradBehandling: ' + intyg.planeradBehandling);
        // luseUtkastPage.planeradBehandling.sendKeys(intyg.planeradBehandling);
        // // logger.info('substansintag: ' + intyg.substansintag);
        // luseUtkastPage.substansintag.sendKeys(intyg.substansintag);

        // luseUtkastPage.medicinskaForutsattningarForArbete.sendKeys(intyg.medicinskaForutsattningarForArbete);
        // luseUtkastPage.aktivitetsFormaga.sendKeys(intyg.aktivitetsFormaga);
        // luseUtkastPage.ovrigt.sendKeys(intyg.ovrigt);
        // luseUtkastPage.kontaktMedFkNo.sendKeys(protractor.Key.SPACE);
        // luseUtkastPage.tillaggsfragor0svar.sendKeys(intyg.tillaggsfragor0svar);
        // luseUtkastPage.tillaggsfragor1svar.sendKeys(intyg.tillaggsfragor1svar)


        //browser.ignoreSynchronization = false;

        // browser.driver.wait(protractor.until.elementIsVisible(luseUtkastPage.signeraButton));

    }
};
