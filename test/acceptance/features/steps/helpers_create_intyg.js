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

/*global intyg,logger,pages,Promise,wcTestTools,person,protractor,browser*/
'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var loginHelpers = require('./inloggning/login.helpers.js');
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var helpers = require('./helpers');


function writeNewIntyg(typ, status) {
    var standardUser = global.user;

    var userObj = {
        forNamn: 'Johan',
        efterNamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        enhetId: standardUser.enhetId,
        lakare: true
    };


    if (typ === 'Läkarintyg FK 7263') {
        logger.silly('Det går inte längre skapa nytt intygs utkast för FK7263');
        return;
    } else {
        // Logga in med en användare som garanterat kan signera intyg
        return loginHelpers.logInAsUserRole(userObj, 'Läkare')
            // Väj samma person som tidigare
            .then(function() {
                return sokSkrivIntygPage.selectPersonnummer(person.id)
                    .then(function() {
                        browser.ignoreSynchronization = false;
                        helpers.tinyDelay();
                    })
                    .then(function() { // Välj rätt typ av utkast
                        logger.silly('Väljer typ av utkast..');
                        sokSkrivIntygUtkastTypePage.createUtkast(helpers.getInternShortcode(typ));
                    })
                    .then(function() {
                        helpers.pageReloadDelay();
                    })
                    .then(function() { // Spara intygsid för kommande steg
                        return browser.getCurrentUrl().then(function(text) {
                            intyg.id = text.split('/').slice(-2)[0];
                            return logger.info('intyg.id: ' + intyg.id);
                        });

                    })
                    .then(function() { // Ange intygsdata
                        logger.silly('Anger intygsdata..');
                        global.intyg = helpers.generateIntygByType(typ, intyg.id);
                        logger.silly(global.intyg);
                        return require('./fillIn').fillIn(intyg);
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
                            return fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE)
                                .then(function() {
                                    logger.silly('Klickar skicka knapp i skicka-dialog..');
                                    return fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE);
                                });
                        } else {
                            logger.silly('Klar utan att skicka till mottagare..');
                            return Promise.resolve();
                        }
                    })
                    .then(function() { // Logga in med tidigare användare
                        logger.silly('Loggar in med tidigare användare..');
                        return loginHelpers.logInAsUser({
                            forNamn: standardUser.forNamn,
                            efterNamn: standardUser.efterNamn,
                            hsaId: standardUser.hsaId,
                            enhetId: standardUser.enhetId,
                            lakare: standardUser.lakare,
                            origin: standardUser.origin
                        });
                    });
            });
    }
}

module.exports = {
    createIntygWithStatus: function(typ, status) {

        intyg.id = testdataHelper.generateTestGuid();
        logger.debug('intyg.id = ' + intyg.id);
        return writeNewIntyg(typ, status);
    }
};
