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
    fillIn: function(intyg) {

        //Returnera Promise kedja
        return new Promise(function(resolve) {
                logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
                browser.ignoreSynchronization = true;
                resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
            })
            .then(function() {
                //Baserat på
                return luseUtkastPage.angeBaseratPa(intyg.baseratPa)
                    .then(function(value) {
                        logger.info('OK - angeBaseratPa: ' + JSON.stringify(intyg.baseratPa));
                    }, function(reason) {
                        throw ('FEL, angeBaseratPa: ' + reason);
                    });
            })
            .then(function() {
                //Andra medicinska utredningar
                return luseUtkastPage.angeAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
                    .then(function(value) {
                        logger.info('OK - angeAndraMedicinskaUtredningar: ' + JSON.stringify(intyg.andraMedicinskaUtredningar));
                    }, function(reason) {
                        throw ('FEL, angeAndraMedicinskaUtredningar: ' + reason);
                    });
            })
            .then(function() {
                //Sjukdomsförlopp
                return luseUtkastPage.angeSjukdomsforlopp(intyg.sjukdomsForlopp)
                    .then(function(value) {
                        logger.info('OK - angeSjukdomsforlopp: ' + JSON.stringify(intyg.sjukdomsForlopp));
                    }, function(reason) {
                        throw ('FEL, angeSjukdomsforlopp: ' + reason);
                    });
            })
            .then(function() {
                //Diagnoser
                return luseUtkastPage.angeDiagnos(intyg.diagnos)
                    .then(function(value) {
                        logger.info('OK - angeDiagnos: ' + JSON.stringify(intyg.diagnos));
                    }, function(reason) {
                        throw ('FEL, angeDiagnos: ' + reason);
                    });
            })
            .then(function() {
                //Funktionsnedsättning
                return luseUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning)
                    .then(function(value) {
                        logger.info('OK - angeFunktionsnedsattning: ' + JSON.stringify(intyg.funktionsnedsattning));
                    }, function(reason) {
                        throw ('FEL, angeFunktionsnedsattning: ' + reason);
                    });
            })
            .then(function() {
                //aktivitetsbegränsning
                return luseUtkastPage.angeAktivitetsbegransning(intyg.aktivitetsbegransning)
                    .then(function(value) {
                        logger.info('OK - angeAktivitetsbegransning: ' + JSON.stringify(intyg.aktivitetsbegransning));
                    }, function(reason) {
                        throw ('FEL, angeAktivitetsbegransning: ' + reason);
                    });
            })
            .then(function() {
                //Medicinsk behandling
                return luseUtkastPage.angeMedicinskBehandling(intyg.medicinskbehandling)
                    .then(function(value) {
                        logger.info('OK - angeMedicinskBehandling: ' + JSON.stringify(intyg.medicinskbehandling));
                    }, function(reason) {
                        throw ('FEL, angeMedicinskBehandling: ' + reason);
                    });
            })
            .then(function() {
                //Medicinska förutsättningar
                return luseUtkastPage.angeMedicinskaForutsattningar(intyg.medicinskaForutsattningar)
                    .then(function(value) {
                        logger.info('OK - angeMedicinskaForutsattningar: ' + JSON.stringify(intyg.medicinskaForutsattningar));
                    }, function(reason) {
                        throw ('FEL, angeMedicinskaForutsattningar: ' + reason);
                    });
            })
            .then(function() {
                //Övriga upplysningar
                return luseUtkastPage.angeOvrigaUpplysningar(intyg.ovrigt)
                    .then(function(value) {
                        logger.info('OK - angeOvrigaUpplysningar: ' + JSON.stringify(intyg.ovrigt));
                    }, function(reason) {
                        throw ('FEL, angeOvrigaUpplysningar: ' + reason);
                    });
            })
            .then(function() {
                //Kontakt med FK
                return luseUtkastPage.angeKontaktMedFK(intyg.kontaktMedFk)
                    .then(function(value) {
                        logger.info('OK - angeKontaktMedFK: ' + JSON.stringify(intyg.kontaktMedFk));
                    }, function(reason) {
                        throw ('FEL, angeKontaktMedFK: ' + reason);
                    });
            })
            .then(function() {
                logger.info('Tilläggsfrågor - TODO: Finns detta kvar?');
                return luseUtkastPage.angeTillaggsfragor(intyg.tillaggsfragor)
                    .then(function(value) {
                        logger.info('OK - angeTillaggsfragor: ' + JSON.stringify(intyg.tillaggsfragor));
                    }, function(reason) {
                        throw ('FEL, angeTillaggsfragor: ' + reason);
                    });
            })
            .then(function() {
                browser.ignoreSynchronization = false;
            });

    }
};
