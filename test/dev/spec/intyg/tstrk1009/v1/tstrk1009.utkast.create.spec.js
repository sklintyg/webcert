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
var testdataHelper = require('common-testtools').testdataHelper;
var specHelper = wcTestTools.helpers.spec;
var UtkastPage = wcTestTools.pages.intyg.ts.trk1009.utkast;
var IntygPage = wcTestTools.pages.intyg.ts.trk1009.intyg;
var restTestdataHelper = wcTestTools.helpers.restTestdata;

describe('Create and Sign tstrk1009 v1 utkast', function() {

    var utkastId = null,
        data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212', 'tstrk1009');
    });

    it('Öppna intyget', function() {

        UtkastPage.disableAutosave();

        specHelper.getUtkastIdFromUrl().then(function(id) {
            utkastId = id;
        });

        data = wcTestTools.testdata.ts.trk1009.v1.get(utkastId);
    });

    describe('Fyll i intyget', function() {
        it('fillInIdentitetStyrktGenom', function() {
            UtkastPage.fillInIdentitetStyrktGenom1009(data.identitetStyrktGenom);
        });

        it('fillAnmalanAvser', function() {
            UtkastPage.fillAnmalanAvser(data.anmalanAvser);
        });

        it('fillMedicinskaForhallanden', function() {
            UtkastPage.fillMedicinskaForhallanden(data.medicinskaForhallanden, data.senasteUndersokningsdatum);
        });

        it('fillBehorigheter', function() {
            UtkastPage.fillBehorigheter(data.intygetAvserBehorigheter);
        });

        it('fillInformationOmTsBeslutOnskas ', function() {
            UtkastPage.enableAutosave();
            UtkastPage.fillInformationOmTsBeslutOnskas(data.informationOmTsBeslutOnskas);
        });

    });

    it('Signera intyget', function() {
        UtkastPage.whenSigneraButtonIsEnabled();

        UtkastPage.signeraButtonClick();

        expect(IntygPage.isAt()).toBeTruthy();
    });

    it('Verifiera intyg', function() {
        IntygPage.verify(data);
    });

    afterAll(function() {
        restTestdataHelper.deleteIntyg(utkastId);
        restTestdataHelper.deleteUtkast(utkastId);
    });
});
