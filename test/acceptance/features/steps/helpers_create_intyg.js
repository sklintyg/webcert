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

/*global intyg,logger,pages,Promise,wcTestTools,user,person,protractor, JSON*/
'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var loginHelpers = require('./inloggning/login.helpers.js');
// var restTestdataHelper = wcTestTools.helpers.restTestdata;
var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var rUtil = wcTestTools.restUtil;
// var intygGenerator = wcTestTools.intygGenerator;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

function createIntygWithRest(intygObj) {

    var userObj = {
        fornamn: user.fornamn,
        efternamn: user.efternamn,
        hsaId: user.hsaId,
        enhetId: user.enhetId,
        lakare: user.lakare,
        forskrivarKod: user.forskrivarKod
    };

    return rUtil.login(userObj).then(function(data) {
        logger.info('Login OK');
        return Promise.resolve('SUCCESS');
    }, function(error) {
        throw ('Login error: ' + error);
    }).then(function() {
        rUtil.createIntyg(intygObj).then(function(response) {
            logger.info('Skapat intyg via REST-api');
            console.log(JSON.parse(response.request.body));
        }, function(error) {
            throw ('Error calling createIntyg' + error);
        });
    });
}


function createTsIntyg(typ, status) {
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
                promiseArr.push(loginHelpers.logInAsUserRole(userObj, standardUser.roleName));
                return Promise.all(promiseArr);
            });
        });
}

function isFKIntyg(typ) {
    return ((typ.indexOf('Läkarintyg') > -1) || (typ.indexOf('Läkarutlåtande') > -1));
}

module.exports = {
    createIntygWithStatus: function(typ, status) {
        //TODO, Hantera ts-intyg

        intyg.id = testdataHelper.generateTestGuid();
        // var signeringsDatum = '2015-04-28T14:00:00.000';
        logger.debug('intyg.id = ' + intyg.id);

        if (typ.indexOf('Transportstyrelsen') > -1) {
            return createTsIntyg(typ, status);

        } else if (isFKIntyg(typ)) {
            var intygObj;

            if (typ === 'Läkarintyg FK 7263') {
                intygObj = intygFromJsonFactory.defaultFK7263();

            } else if (typ === 'Läkarutlåtande för sjukersättning') {
                intygObj = intygFromJsonFactory.defaultLuse();

            } else if (typ === 'Läkarintyg för sjukpenning utökat') {
                //intygObj = intygFromJsonFactory.defaultLisu();
                throw ('TODO: Skapa LISU via REST');
            }
            var intygDoc = JSON.parse(intygObj.document);
            intygDoc.grundData.patient.personId = person.id;
            intygDoc.grundData.patient.personId = person.id;
            intygDoc.grundData.skapadAv.vardenhet.enhetsid = user.enhetId;
            intygDoc.grundData.skapadAv.personId = user.hsaId;

            intygObj.signingDoctorName = user.fornamn + ' ' + user.efternamn;
            intygObj.careUnitId = user.enhetId;
            intygObj.civicRegistrationNumber = person.id;

            intygObj.revoked = (status === 'Makulerat');

            intygObj.certificateStates = [{
                target: 'HV',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }];
            if (status === 'Mottaget' || status === 'Makulerat') {
                intygObj.certificateStates.push({
                    state: 'SENT',
                    target: 'FK',
                    timestamp: '2016-08-05T14:31:03.227'
                });
            }
            intygObj.document = JSON.stringify(intygDoc);
            return createIntygWithRest(intygObj);

        } else {
            throw ('TODO: Hantera fall då det inte redan finns något intyg att använda');
        }
    }
};
