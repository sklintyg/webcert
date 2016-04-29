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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luaeFS.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var luaefsTemplate = require('webcert-testtools/testdata/luae_fs-minimal.json');
var restUtil = require('webcert-testtools/util/rest.util.js');
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

// Use fdescribe to run in isolation.
describe('Validera makulering av luae_fs Intyg', function() {

    var intygsId = 'intyg-visa-test-1';

    function buildLuaefsIntyg() {
        var certHolder = {
            id: intygsId,
            document: JSON.stringify(luaefsTemplate),
            originalCertificate: '',
            type: 'luae_fs',
            signingDoctorName: 'Jan Nilsson',
            careUnitId: 'IFV1239877878-1042',
            careUnitName: 'WebCert Enhet 1',
            careGiverId: 'IFV1239877878-1041',
            civicRegistrationNumber: '19121212-1212',
            signedDate: '2016-04-28',
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HV',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
        return certHolder;
    }

    function isCancelled(states) {
        for (var a = 0; a < states.length; a++) {
            if (states[a].state === 'CANCELLED') {
                return true;
            }
        }
        return false;
    }

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Something...', function() {
            restUtil.createIntyg(buildLuaefsIntyg());
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Makulera intyget', function() {
            IntygPage.makulera.btn.sendKeys(protractor.Key.SPACE);
            browser.wait(IntygPage.makulera.dialogAterta.isDisplayed())
                .then(IntygPage.makulera.dialogAterta.sendKeys(protractor.Key.SPACE));

            browser.wait(IntygPage.makulera.kvittensOKBtn.isDisplayed())
                .then(IntygPage.makulera.kvittensOKBtn.sendKeys(protractor.Key.SPACE));

            element.all(by.id('#makuleraBtn')).then(function(items) {
                expect(items.length).toBe(0);
            });
        });

    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = true;
    });

});
