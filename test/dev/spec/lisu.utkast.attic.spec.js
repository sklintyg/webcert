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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LisuUtkastPage = wcTestTools.pages.intyg.lisu.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;

describe('Lisu attic tests', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        testdataHelper.createUtkast('lisu').then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;

            var utkastData = JSON.parse(intygGenerator.buildIntyg({
                intygType: 'lisu',
                intygId: intygsId,
                personnr: utkast.patientPersonnummer
            }).document);

            testdataHelper.saveUtkast('lisu', intygsId, utkast.version, utkastData, function(){
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
    });

    it('should load utkast', function () {
        LisuUtkastPage.get(intygsId);
        LisuUtkastPage.showMissingInfoButtonClick();

        expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
    });

    describe('annat', function() {
        it('should still be valid if annat is empty', function() {
            LisuUtkastPage.baseratPa.annat.checkbox.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('');
            expect(LisuUtkastPage.baseratPa.annat.beskrivning.isPresent()).toBeFalsy();
            // annatBeskrivning should be removed from the model sent to the server
            // if it is still present we should get a validationerror here.
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it ('should restore annatBeskrivning if annat is specified again', function() {
            LisuUtkastPage.baseratPa.annat.datum.sendKeys('2016-12-12');
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('2016-12-12');
            expect(LisuUtkastPage.baseratPa.annat.beskrivning.getAttribute('value')).toBe('Annat underlag');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('sysselsättning', function() {
        it('should still be valid if changed to arbetssökande', function() {
            LisuUtkastPage.sysselsattning.typ.arbetssokande.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.sysselsattning.arbetsmarknadspolitisktProgramBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it ('should restore nuvarandeArbete description if nuvarande arbete is selected again', function() {
            LisuUtkastPage.sysselsattning.typ.nuvarandeArbete.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.getAttribute('value')).toBe('defenestrist');
            expect(LisuUtkastPage.sysselsattning.arbetsmarknadspolitisktProgramBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should still be valid if changed to Deltar i arbetmarknadspolitiskt program', function() {
            LisuUtkastPage.sysselsattning.typ.arbetmarknadspolitisktProgram.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.sysselsattning.arbetsmarknadspolitisktProgramBeskrivning.sendKeys('Bra program');
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should still be valid if changed to studier', function() {
            LisuUtkastPage.sysselsattning.typ.studier.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.sysselsattning.arbetsmarknadspolitisktProgramBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it ('should restore arbetsmarknadspolitisktProgram description if arbetmarknadspolitisktProgram is selected again', function() {
            LisuUtkastPage.sysselsattning.typ.arbetmarknadspolitisktProgram.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sysselsattning.nuvarandeArbeteBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.sysselsattning.arbetsmarknadspolitisktProgramBeskrivning.getAttribute('value')).toBe('Bra program');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('sjukskrivningar', function() {
        it('should still be valid if sjukskrivningsperiod > 75', function() {
            // Split into 2 it blocks to speed up clearing of these fields with ignoreSynchronization
            browser.ignoreSynchronization = true;
            LisuUtkastPage.sjukskrivning[75].fran.clear();
            LisuUtkastPage.sjukskrivning[75].till.clear();
            LisuUtkastPage.sjukskrivning[50].fran.clear();
            LisuUtkastPage.sjukskrivning[50].till.clear();
            LisuUtkastPage.sjukskrivning[25].fran.clear();
            LisuUtkastPage.sjukskrivning[25].till.clear();
        });

        it('should still be valid if sjukskrivningsperiod > 75', function() {
            browser.ignoreSynchronization = false;
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.nej.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.ja.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.beskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore all arbetstidsforlaggning fields if sjukskrivning 75% is selected again', function() {
            LisuUtkastPage.sjukskrivning[75].fran.sendKeys('2016-05-09');
            LisuUtkastPage.sjukskrivning[75].till.sendKeys('2016-05-15');
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.ja.getAttribute('checked')).toBeTruthy();
            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.beskrivning.getAttribute('value')).toBe('Nattetid är bäst');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should still be valid if sjukskrivningsperiod > 75', function() {
            LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.nej.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.beskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore arbetstidsforlaggning description if arbetstidsforlaggning ja is selected again', function() {
            LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.ja.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.sjukskrivning.arbetstidsforlaggning.beskrivning.getAttribute('value')).toBe('Nattetid är bäst');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        describe('prognos', function() {
            it('should still be valid if prognos is changed', function() {
                LisuUtkastPage.sjukskrivning.prognos.typ[1].sendKeys(protractor.Key.SPACE);
                LisuUtkastPage.showMissingInfoButtonClick(true);

                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[30].isPresent()).toBeFalsy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[60].isPresent()).toBeFalsy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[90].isPresent()).toBeFalsy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[180].isPresent()).toBeFalsy();
                expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
            });

            it('should restore dagarTillArbete if prognos x days is selected again', function() {
                LisuUtkastPage.sjukskrivning.prognos.typ[5].sendKeys(protractor.Key.SPACE);
                LisuUtkastPage.showMissingInfoButtonClick(true);

                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[30].isPresent()).toBeTruthy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[60].isPresent()).toBeTruthy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[90].isPresent()).toBeTruthy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[180].isPresent()).toBeTruthy();
                expect(LisuUtkastPage.sjukskrivning.prognos.dagarTillArbete[60].getAttribute('checked')).toBeTruthy();
                expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
            });
        });
    });

    describe('åtgärder', function() {
        it('should still be valid if åtgärd is changed to "Inte aktuellt"', function() {
            LisuUtkastPage.atgarder.typ[8].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.atgarder.typ[1].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderEjAktuelltBeskrivning.sendKeys('Inte längre aktuellt');
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.atgarder.typ[8].isEnabled()).toBe(false);
            expect(LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderAktuelltBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore arbetslivsinriktadeAtgarderAktuelltBeskrivning if åtgärd "Arbetsträning" is selected', function() {
            LisuUtkastPage.atgarder.typ[1].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.atgarder.typ[2].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.atgarder.typ[1].isEnabled()).toBe(false);
            expect(LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderAktuelltBeskrivning.getAttribute('value')).toBe('Det är alltid bra');
            expect(LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderEjAktuelltBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore arbetslivsinriktadeAtgarderAktuelltBeskrivning if åtgärd "Inte längre aktuellt" is selected again', function() {
            LisuUtkastPage.atgarder.typ[2].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.atgarder.typ[1].sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.atgarder.typ[2].isEnabled()).toBe(false);
            expect(LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderAktuelltBeskrivning.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.atgarder.arbetslivsinriktadeAtgarderEjAktuelltBeskrivning.getAttribute('value')).toBe('Inte längre aktuellt');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('kontaktMedFk', function() {
        it('should still be valid if kontaktMedFk is set to no', function() {
            LisuUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.anledningTillKontakt.isPresent()).toBeFalsy();
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore anledningTillKontakt if kontaktMedFk is set to yes again', function() {
            LisuUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);
            LisuUtkastPage.showMissingInfoButtonClick(true);

            expect(LisuUtkastPage.anledningTillKontakt.getAttribute('value')).toBe('Egentligen inte');
            expect(LisuUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });
});
