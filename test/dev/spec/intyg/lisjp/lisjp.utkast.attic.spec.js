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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LisjpUtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;

describe('Lisjp attic tests', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        testdataHelper.createUtkast('lisjp').then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;

            var utkastData = JSON.parse(intygGenerator.buildIntyg({
                intygType: 'lisjp',
                intygId: intygsId,
                personnr: utkast.patientPersonnummer
            }).document);

            testdataHelper.saveUtkast('lisjp', intygsId, utkast.version, utkastData, function(){
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
    });

    it('should load utkast', function () {
        LisjpUtkastPage.get(intygsId);
        LisjpUtkastPage.disableAutosave();
    });

    describe('annat', function() {
        it('should still be valid if annat is empty', function() {
            LisjpUtkastPage.baseratPa.annat.checkbox.sendKeys(protractor.Key.SPACE);

            expect(LisjpUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('');
            expect(LisjpUtkastPage.baseratPa.annat.beskrivning.isPresent()).toBeFalsy();
            // annatBeskrivning should be removed from the model sent to the server
            // if it is still present we should get a validationerror here.
        });

        it ('should restore annatBeskrivning if annat is specified again', function() {
            LisjpUtkastPage.baseratPa.annat.datum.sendKeys('2016-12-12');

            expect(LisjpUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('2016-12-12');
            expect(LisjpUtkastPage.baseratPa.annat.beskrivning.getAttribute('value')).toBe('Annat underlag');
        });
    });

    describe('kontaktMedFk', function() {
        it('should still be valid if kontaktMedFk is set to no', function() {
            LisjpUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);

            expect(LisjpUtkastPage.anledningTillKontakt.isPresent()).toBeFalsy();
        });

        it('should restore anledningTillKontakt if kontaktMedFk is set to yes again', function() {
            LisjpUtkastPage.enableAutosave();
            LisjpUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);

            expect(LisjpUtkastPage.anledningTillKontakt.getAttribute('value')).toBe('Egentligen inte');
        });
    });

});
