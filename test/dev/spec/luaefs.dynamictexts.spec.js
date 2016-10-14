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
var textHelper = wcTestTools.helpers.fkTextHelper;
var UtkastPage = wcTestTools.pages.intyg.luae_fs.utkast;
var IntygPage = wcTestTools.pages.intyg.luae_fs.intyg;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var restUtil = wcTestTools.restUtil;

describe('Create luae_fs utkast and check dynamic texts', function() {

    var utkast = null;
    var intyg = null;

    var texts = null;


    beforeAll(function() {
        testdataHelper.createUtkast('luae_fs').then(function(response) {
            utkast = response.body;
            expect(utkast.intygsId).not.toBeNull();
        }, function(error) {
            console.log('Error calling createUtkast' + error);
        });

        //Load and cache expected dynamictext-values for this intygstype.
        textHelper.readTextsFromFkTextFile('texterMU_LUAE_FS_v1.0.xml').then(function(textResources) {
            texts = textResources;
        }, function(err) {
            fail('Error during text lookup ' + err);
        });

    });

    describe('Check dynamic labels', function() {
        it('should login and open created utkast', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            UtkastPage.get(utkast.intygsId);
        });

        it('should have dynamic texts on luae_fs draft', function() {

            //Min undersökning av patienten
            expect(UtkastPage.getDynamicLabelText('KV_FKMU_0001.UNDERSOKNING.RBK')).toBe(texts['KV_FKMU_0001.UNDERSOKNING.RBK']);

            //Funktionsnedsättning/påverkan
            expect(UtkastPage.getDynamicLabelText('FRG_16.RBK')).toBe(texts['FRG_16.RBK']);

            //Tilläggsfråga
            expect(UtkastPage.getDynamicLabelText('DFR_9001.1.RBK')).toBe(texts['DFR_9001.1.RBK']);

        });


    });

    describe('Verify dynamic texts on luae_fs certificate', function() {

        it('creates certificate via testabilityAPI...', function() {
            intyg = intygFromJsonFactory.defaultLuaefs();
            restUtil.createIntyg(intyg);

            IntygPage.get(intyg.id);
            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('should have dynamic texts on certificate', function() {
            //Min undersökning av patienten
            expect(IntygPage.getDynamicLabelText('KV_FKMU_0001.UNDERSOKNING.RBK')).toBe(texts['KV_FKMU_0001.UNDERSOKNING.RBK']);

            //Funktionsnedsättning/påverkan
            expect(IntygPage.getDynamicLabelText('FRG_16.RBK')).toBe(texts['FRG_16.RBK']);

            //Tilläggsfråga
            expect(IntygPage.getDynamicLabelText('DFR_9001.1.RBK')).toBe(texts['DFR_9001.1.RBK']);
        });



    });

    afterAll(function() {
        testdataHelper.deleteUtkast(utkast.intygsId);
        testdataHelper.deleteIntyg(intyg.id);
        specHelper.logout();
    });

});
