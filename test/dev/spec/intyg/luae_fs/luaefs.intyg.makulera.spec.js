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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luaeFS.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('Validera makulering av luae_fs Intyg', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Skapa signerat intyg i IT, visa intyget', function() {
            var intyg = intygFromJsonFactory.defaultLuaefs();
            intygsId = intyg.id;
            restUtil.createIntyg(intyg);
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Makulera intyget', function() {
            IntygPage.makulera.btn.sendKeys(protractor.Key.SPACE);
            IntygPage.makulera.dialogRadioFelPatient.sendKeys(protractor.Key.SPACE);
            expect(IntygPage.makulera.dialogMakulera.isDisplayed()).toBeTruthy();

            IntygPage.makulera.dialogMakulera.sendKeys(protractor.Key.SPACE);
        });

        it('check intyg is makulerat', function() {
            logger.debug("sleeping a while to allow changes to have taken effect in backend before checking revoked status");
            browser.sleep(2000).then(function() {
                expect(isIntygRevoked(intygsId)).toBeTruthy();
                expect(IntygPage.makulera.btn.isPresent()).toBeFalsy();
            });

        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = false;
    });

});

function isIntygRevoked(intygsId) {
    var innerDefer = protractor.promise.defer();
    restUtil.getIntyg(intygsId).then(function(intygBody) {
        var result = IntygPage.hasState(intygBody.body.states, 'CANCELLED');
        innerDefer.fulfill(result);
    });
    return innerDefer.promise;
}
