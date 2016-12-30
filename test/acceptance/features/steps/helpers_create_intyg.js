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

/*global intyg,logger,pages,Promise,wcTestTools,person,protractor*/
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
        fornamn: 'Erik',
        efternamn: 'Nilsson',
        hsaId: 'TSTNMT2321000156-105H',
        enhetId: standardUser.enhetId,
        lakare: true
    };
    return loginHelpers.logInAsUserRole(userObj, 'Läkare')
        .then(function() {
            sokSkrivIntygPage.selectPersonnummer(person.id);
            sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(typ);
            sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE);
            global.intyg = require('./helpers').generateIntygByType(typ);
            return require('./fillIn').fillIn(intyg).then(function() {
                var promiseArr = [];
                var userObj = {
                    fornamn: standardUser.fornamn,
                    efternamn: standardUser.efternamn,
                    hsaId: standardUser.hsaId,
                    enhetId: standardUser.enhetId,
                    lakare: standardUser.lakare,
                    origin: standardUser.origin
                };

                promiseArr.push(fkUtkastPage.signeraButton.sendKeys(protractor.Key.SPACE));
                if (status === 'Mottaget') {
                    promiseArr.push(fkIntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE));
                    promiseArr.push(fkIntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE));
                }
                promiseArr.push(loginHelpers.logInAsUser(userObj));
                return Promise.all(promiseArr);
            });
        });
}

module.exports = {
    createIntygWithStatus: function(typ, status) {

        intyg.id = testdataHelper.generateTestGuid();
        logger.debug('intyg.id = ' + intyg.id);
        return writeNewIntyg(typ, status);
    }
};
