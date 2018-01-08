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

/*globals describe,it,browser */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var WelcomePage = wcTestTools.pages.welcome;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var restUtil = wcTestTools.restUtil;

describe('Testa enkelt flöde för sekretessmarkerad vårdpersonal', function() {

    var sekretessDialogCheckbox = element(by.id('wc-vardperson-sekretess-modal-dialog--checkbox'));
    var sekretessDialogOKBtn = element(by.id('wc-vardperson-sekretess-modal-dialog--consent-btn'));

    var sekretessInfoDialogLink = element(by.id('wc-vardperson-sekretess-info-dialog--link'));
    var sekretessInfoDialogOkBtn = element(by.id('wc-vardperson-sekretess-info-dialog-confirmationOkButton'));



    beforeAll(function() {
        //Säkerställ att det inte finns ett sparat dialoggodkännande för denna användaren
        restUtil.deleteAnvandarPreference('TSTNMT2321000156-1099', 'wc.vardperson.sekretess.approved');
    });


    it('Logga in med en sekretessmarkerad användare', function() {
        WelcomePage.get();
        expect(WelcomePage.isAt()).toBeTruthy();
        //Logga in som "Sara Sekretess"
        WelcomePage.login('TSTNMT2321000156-1099_TSTNMT2321000156-1077', false);
        specHelper.waitForAngularTestability();
        expect(SokSkrivIntygPage.isAt()).toBeTruthy();
        expect(SokSkrivIntygPage.getDoctorText()).toContain('Sara Sekretess');

        expect(sekretessInfoDialogLink.isDisplayed()).toBe(true);
        //Dialog med checkbox skall visas - men vara disabled
        expect(sekretessDialogCheckbox.isDisplayed()).toBe(true);
        expect(sekretessDialogOKBtn.isDisplayed()).toBe(true);
        expect(sekretessDialogOKBtn.isEnabled()).toBe(false);
    });


    it('Bocka i godkännande och då skall OK enablas', function() {
        sekretessDialogCheckbox.sendKeys(protractor.Key.SPACE);

        expect(sekretessDialogOKBtn.isEnabled()).toBe(true);
    });

    it('Tryck OK och sekretessgodkännandedialogen skall stängas', function() {
        sekretessDialogOKBtn.sendKeys(protractor.Key.SPACE);
        expect(sekretessDialogOKBtn).toDisappear();
    });

    it('Klicka på sekretessinfo länken, så skall sekretessinfo modalen visas', function() {
        sekretessInfoDialogLink.sendKeys(protractor.Key.ENTER);
        expect(sekretessInfoDialogOkBtn.isPresent()).toBe(true);
    });

    it('Klicka OK sekretessinfo modalen, så skall den stängas', function() {
        sekretessInfoDialogOkBtn.sendKeys(protractor.Key.SPACE);
        expect(sekretessInfoDialogOkBtn).toDisappear();
    });

});
