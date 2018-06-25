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

/* globals pages, logger, JSON, Promise, browser, protractor */

'use strict';
var utkastPage;
var helpers = require('../helpers');
module.exports = {
    fillInEnhetAdress: function(user) {
        if (!user.enhetsAdress) {
            logger.warn('Enhetsaddress ändras inte');
            return new Promise(resolve => resolve());
        }
        return utkastPage.angeEnhetAdress(user.enhetsAdress)
            .then(function() {
                logger.info('OK - angeEnhetAdress :' + JSON.stringify(user.enhetsAdress));
            }, function(reason) {
                throw ('FEL, angeEnhetAdress,' + reason);
            });
    },
    setPatientAdressIfNotGiven: function(world) {
        var isSMI = helpers.isSMIIntyg(world.intyg.typ);

        // Regler som bör stämmas av med krav:
        // SMI har inte patientadress i och med sekretessmarkering
        // TS har patientadress
        // DO/DOI har patientadress, men bara om patientadress saknas i PU (våra test-patienter har inte adress i PU för DB/DOI)
        // Djupintegration har inte adress

        utkastPage = pages.getUtkastPageByType(world.intyg.typ);

        if (world.patient.adress && world.patient.adress.postadress && !isSMI && world.user.origin !== 'DJUPINTEGRATION') {
            return utkastPage.angePatientAdress(world.patient.adress).then(function() {
                logger.info('OK - setPatientAdress: ' + JSON.stringify(world.patient.adress));
            }, function(reason) {
                throw ('FEL - setPatientAdress: ' + reason);
            }).catch(msg => logger.warn(msg));
        } else {
            logger.info('Ingen patientadress ändras');
            if (!isSMI && world.user.origin !== 'DJUPINTEGRATION') {
                world.patient.adress = {};
                return Promise.all([
                    utkastPage.patientAdress.postAdress.getText().then(function(text) {
                        world.patient.adress.postadress = text;
                    }),
                    utkastPage.patientAdress.postNummer.getText().then(function(text) {
                        world.patient.adress.postnummer = text;
                    }),
                    utkastPage.patientAdress.postOrt.getText().then(function(text) {
                        world.patient.adress.postort = text;
                    })
                ]);
            } else {
                return Promise.resolve();
            }
        }

    },
    fillIn: function(world) {
        utkastPage = pages.getUtkastPageByType(world.intyg.typ);
        return this.fillInEnhetAdress(world.user)
            .then(() => this.setPatientAdressIfNotGiven(world))
            .then(() => browser.driver.switchTo().activeElement().sendKeys(protractor.Key.TAB));
    }

};
