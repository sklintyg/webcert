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

/*global logger,pages,Promise,wcTestTools, protractor,browser*/
'use strict';
let testTools = require('common-testtools');
testTools.protractorHelpers.init();
var testdataHelper = wcTestTools.helpers.testdata;
var loginHelpers = require('./inloggning/login.helpers.js');
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var helpers = require('./helpers');


function writeNewIntyg(world, status) {
    if (world.intyg.typ === 'Läkarintyg FK 7263') {
        throw ('Det går inte längre skapa nytt intygs utkast för FK7263');
    } else {
        logger.silly('Väljer typ av utkast..');
        return sokSkrivIntygUtkastTypePage.createUtkast(helpers.getInternShortcode(world.intyg.typ))
            .then(function() {
                return helpers.pageReloadDelay();
            })
            .then(function() { // Spara intygsid för kommande steg
                return browser.getCurrentUrl().then(function(text) {
                    world.intyg.id = text.split('/').slice(-2)[0];
                    return logger.info('intyg.id: ' + world.intyg.id);
                });

            })
            .then(function() { // Ange intygsdata
                logger.silly('Anger intygsdata..');
                world.intyg = helpers.generateIntygByType(world.intyg, world.patient);
                logger.silly(world.intyg);
                return require('./fillIn').fillIn(world);
            })
            .then(function() { //Klicka på signera
                logger.silly('Klickar på signera..');
                return fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
            })
            .then(function() {
                return helpers.largeDelay();
            })
            .then(function() { // Skicka till mottagare om intyget ska vara Skickat
                if (status === 'Skickat') {
                    logger.silly('Klickar på skicka knapp..');
                    return fkIntygPage.skicka.knapp.typeKeys(protractor.Key.SPACE)
                        .then(function() {
                            logger.silly('Klickar skicka knapp i skicka-dialog..');
                            return fkIntygPage.skicka.dialogKnapp.typeKeys(protractor.Key.SPACE);
                        });
                } else {
                    logger.silly('Klar utan att skicka till mottagare..');
                    return Promise.resolve();
                }
            });
    }
}

module.exports = {
    createIntygWithStatus: function(world, status) {

        world.intyg.id = testdataHelper.generateTestGuid();
        logger.debug('intyg.id = ' + world.intyg.id);


        let previousUser;
        if (!world.user.lakare) {
            previousUser = Object.create(world.user);
            world.user = {
                forNamn: 'Johan',
                efterNamn: 'Johansson',
                hsaId: 'TSTNMT2321000156-107V',
                enhetId: world.user.enhetId,
                lakare: true
            };
            return loginHelpers.logInAsUserRole(world.user, 'Läkare')
                .then(function() {
                    logger.silly('Väjer samma person som tidigare..');
                    return sokSkrivIntygPage.selectPersonnummer(world.patient.id);
                })
                .then(function() {
                    browser.ignoreSynchronization = false;
                    return helpers.tinyDelay();
                })
                .then(function() {
                    logger.silly('Fyller i utkast och signerar..');
                    return writeNewIntyg(world, status);
                })
                .then(function() {
                    logger.silly('Loggar in med tidigare användare..');
                    world.user = previousUser;
                    return loginHelpers.logInAsUser(world.user);

                });
        } else {
            return writeNewIntyg(world, status);
        }
    }
};
