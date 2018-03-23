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

/* globals pages, logger, JSON, Promise,intyg, browser */

'use strict';
var utkastPage;
var helpers = require('../helpers');
module.exports = {
    fillInEnhetAdress: function() {
        return utkastPage.angeEnhetAdress(global.user.enhetsAdress)
            .then(function() {
                logger.info('OK - angeEnhetAdress :' + JSON.stringify(global.user.enhetsAdress));
            }, function(reason) {
                throw ('FEL, angeEnhetAdress,' + reason);
            });
    },
    setPatientAdressIfNotGiven: function() {
        var isSMI = helpers.isSMIIntyg(global.intyg.typ);

        // Regler som bör stämmas av med krav:
        // SMI har inte patientadress i och med sekretessmarkering
        // TS har patientadress
        // DO/DOI har patientadress, men bara om patientadress saknas i PU (våra test-patienter har inte adress i PU för DB/DOI)
        // Djupintegration har inte adress

        utkastPage = pages.getUtkastPageByType(intyg.typ);
        if (global.person.adress && global.person.adress.postadress && !isSMI && global.user.origin !== 'DJUPINTEGRATION') {
            return utkastPage.angePatientAdress(global.person.adress).then(function() {
                logger.info('OK - setPatientAdress :' + JSON.stringify(global.person.adress));
            }, function(reason) {
                throw ('FEL, setPatientAdress,' + reason);
            });
        } else {
            logger.info('Ingen patientadress ändras');
            if (!isSMI && global.user.origin !== 'DJUPINTEGRATION') {
                global.person.adress = {};
                return Promise.all([
                    utkastPage.patientAdress.postAdress.getText().then(function(text) {
                        global.person.adress.postadress = text;
                    }),
                    utkastPage.patientAdress.postNummer.getText().then(function(text) {
                        global.person.adress.postnummer = text;
                    }),
                    utkastPage.patientAdress.postOrt.getText().then(function(text) {
                        global.person.adress.postort = text;
                    })
                ]);
            } else {
                return Promise.resolve();
            }
        }

    },
    fillIn: function(intyg) {
        utkastPage = pages.getUtkastPageByType(intyg.typ);
        return this.fillInEnhetAdress().then(() => this.setPatientAdressIfNotGiven()).then(() => browser.driver.switchTo().activeElement());
    }

};
