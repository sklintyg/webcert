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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;

describe('Luse diagnos tests', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        testdataHelper.createUtkast('luse').then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;

            var utkastData = JSON.parse(intygGenerator.buildIntyg({
                intygType: 'luse',
                intygId: intygsId,
                personnr: utkast.patientPersonnummer
            }).document);

            testdataHelper.saveUtkast('luse', intygsId, utkast.version, utkastData, function(){
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
    });

    it('should load utkast', function () {
        LuseUtkastPage.get(intygsId);
        LuseUtkastPage.disableAutosave();
    });

    it ('should not clear diagnoskod if 4-character psykiskdiagnos is entered', function() {
        LuseUtkastPage.diagnos.diagnosRow(0).kod.clear();
        LuseUtkastPage.diagnos.diagnosRow(0).kod.sendKeys('Z730');
        LuseUtkastPage.diagnos.diagnosRow(0).kod.sendKeys(protractor.Key.TAB, protractor.Key.TAB);
        expect(LuseUtkastPage.diagnos.diagnosRow(0).kod.getAttribute('value')).toEqual('Z730');
        expect(LuseUtkastPage.diagnos.diagnosRow(0).beskrivning.getAttribute('value')).toEqual('Utbrändhet');
    });

    it ('should clear diagnoskod if 3-character psykiskdiagnos is entered', function() {
        LuseUtkastPage.diagnos.diagnosRow(0).kod.clear();
        LuseUtkastPage.enableAutosave();
        LuseUtkastPage.diagnos.diagnosRow(0).kod.sendKeys('Z73', protractor.Key.TAB, protractor.Key.TAB);
        expect(LuseUtkastPage.diagnos.diagnosRow(0).kod.getAttribute('value')).toEqual('');
        expect(LuseUtkastPage.diagnos.diagnosRow(0).beskrivning.getAttribute('value')).toEqual('');
    });


});
