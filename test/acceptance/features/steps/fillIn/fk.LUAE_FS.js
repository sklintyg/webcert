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

        //Returnera Promise kedja
        return new Promise(function(resolve) {
                logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
                browser.ignoreSynchronization = true;
                resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
            })
            .then(function() {
                //Baserat på
                return luaefsUtkastPage.angeBaseratPa(intyg.baseratPa)
                    .then(function(value) {
                        logger.info('Baseras på: ' + JSON.stringify(intyg.baseratPa));
                    }, function(reason) {
                        throw ('FEL, Baseras på: ' + reason);
                    });
            })
            .then(function() {
                //Andra medicinska utredningar
                return luaefsUtkastPage.angeAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
                    .then(function(value) {
                        logger.info('Andra medicinska utredningar: ' + JSON.stringify(intyg.andraMedicinskaUtredningar));
                    }, function(reason) {
                        throw ('FEL, Andra medicinska utredningar: ' + reason);
                    });
            })
            .then(function() {
                //Diagnoser
                return luaefsUtkastPage.angeDiagnos(intyg.diagnos)
                    .then(function(value) {
                        logger.info('Diagnos: ' + JSON.stringify(intyg.diagnos));
                    }, function(reason) {
                        throw ('FEL, Diagnos: ' + reason);
                    });
            })
            .then(function() {
                //Funktionsnedsättning
                return luaefsUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning)
                    .then(function(value) {
                        logger.info('Funktionsnedsättning: ' + JSON.stringify(intyg.funktionsnedsattning));
                    }, function(reason) {
                        throw ('FEL, Funktionsnedsättning: ' + reason);
                    });
            })
            .then(function() {
                //Övriga upplysningar
                return luaefsUtkastPage.angeOvrigaUpplysningar(intyg.ovrigt)
                    .then(function(value) {
                        logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.ovrigt));
                    }, function(reason) {
                        throw ('FEL, Övriga upplysningar: ' + reason);
                    });
            })
            .then(function() {
                //Kontakt med FK
                return luaefsUtkastPage.angeKontaktMedFK(intyg.kontaktMedFk)
                    .then(function(value) {
                        logger.info('Övriga upplysningar: ' + JSON.stringify(intyg.kontaktMedFk));
                    }, function(reason) {
                        throw ('FEL, Övriga upplysningar: ' + reason);
                    });
            })
            .then(function() {
                browser.ignoreSynchronization = false;
            });
    }
};
