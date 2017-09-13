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

/*global intyg,logger,pages,Promise,wcTestTools,person,protractor,browser*/
'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var loginHelpers = require('./inloggning/login.helpers.js');
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;


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
        console.log('Det går inte längre skapa nytt intygs utkast för FK7263');
        return sokSkrivIntygPage.selectPersonnummer(person.id);
    } else {
        // Logga in med en användare som garanterat kan signera intyg
        return loginHelpers.logInAsUserRole(userObj, 'Läkare')
            // Väj samma person som tidigare
            .then(function() {
                return sokSkrivIntygPage.selectPersonnummer(person.id)
                    .then(function() { // Välj rätt typ av utkast
                        console.log('Väljer typ av utkast..');
                        return sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(typ);
                    })
                    .then(function() { // Klicka på skapa nytt utkast
                        console.log('Klickar på nytt utkast knapp');
                        return sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);
                    })
                    .then(function() {
                        return browser.sleep(6000);
                    })
                    .then(function() { // Spara intygsid för kommande steg
                        return browser.getCurrentUrl().then(function(text) {
                            intyg.id = text.split('/').slice(-2)[0];
                            return logger.info('intyg.id: ' + intyg.id);
                        });

                    })
                    .then(function() { // Ange intygsdata
                        console.log('Anger intygsdata..');
                        global.intyg = require('./helpers').generateIntygByType(typ, intyg.id);
                        console.log(global.intyg);
                        return require('./fillIn').fillIn(intyg);
                    })
                    .then(function() { //Klicka på signera
                        console.log('Klickar på signera..');
                        return fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE);
                    })
                    .then(function() {
                        return browser.sleep(2000);
                    })
                    .then(function() { // Skicka till mottagare om intyget ska vara Skickat
                        if (status === 'Skickat') {
                            console.log('Klickar på skicka knapp..');
                            return fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE)
                                .then(function() {
                                    console.log('Klickar skicka knapp i skicka-dialog..');
                                    return fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE);
                                });
                        } else {
                            console.log('Klar utan att skicka till mottagare..');
                            return Promise.resolve();
                        }
                    })
                    .then(function() { // Logga in med tidigare användare
                        console.log('Loggar in med tidigare användare..');
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
