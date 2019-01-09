/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/* globals logger, pages, browser, Promise */


var af00213UtkastPage = pages.intyg.af.af00213.utkast;
//var af00213UtkastPage = pages.intyg.af['00213'].utkast;
module.exports = {
    fillIn: function(intyg) {
        'use strict';
        //Returnera Promise kedja
        return new Promise(function(resolve) {
            logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
            browser.ignoreSynchronization = true;
            resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
        }).then(function() {
            return af00213UtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning).then(function() {
                logger.info('OK - angeFunktionsnedsattning');
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, angeFunktionsnedsattning ' + reason);
            });
        }).then(function() {
            if (typeof(intyg.aktivitetsbegransning) !== 'undefined') {
                return af00213UtkastPage.angeAktivitetsbegransning(intyg.aktivitetsbegransning).then(function() {
                    logger.info('OK - angeAktivitetsbegransning');
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeAktivitetsbegransning,' + reason);
                });
            }
        }).then(function() {
            if (typeof(intyg.utredningBehandling) !== 'undefined') {
                return af00213UtkastPage.angeUtredningBehandling(intyg.utredningBehandling).then(function() {
                    logger.info('OK - angeUtredningBehandling');
                }, function(reason) {
                    console.trace(reason);
                    throw ('FEL, angeUtredningBehandling,' + reason);
                });
            }
        }).then(function() {
            return af00213UtkastPage.angeArbetetsPaverkan(intyg.arbetetsPaverkan).then(function() {
                logger.info('OK - angeArbetetsPaverkan');
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, angeArbetetsPaverkan,' + reason);
            });
        }).then(function() {
            return af00213UtkastPage.angeOvrigaUpplysningar(intyg.ovrigt).then(function() {
                logger.info('OK - angeOvrigaUpplysningar');
            }, function(reason) {
                console.trace(reason);
                throw ('FEL, angeOvrigaUpplysningar,' + reason);
            });
        });

    }
};
