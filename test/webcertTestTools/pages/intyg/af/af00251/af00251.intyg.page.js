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

'use strict';

var AfBaseIntyg = require('../af.base.intyg.page.js');
var testdataHelper = require('common-testtools').testdataHelper;


var EJ_ANGIVET = "Ej angivet";

var Af00251Intyg = AfBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'af00251';
        this.intygTypeVersion = '1.0';

        this.minUndersokning = element(by.id('undersokningsDatum'));
        this.annat = {
            datum: element(by.id('annatDatum')),
            text: element(by.id('annatBeskrivning')),
        };

        this.arbetsmarknadspolitisktProgram = {
            text: element(by.id('arbetsmarknadspolitisktProgram-medicinskBedomning')),
            omfattning: element(by.id('arbetsmarknadspolitisktProgram-omfattning')),
            deltidText: element(by.id('arbetsmarknadspolitisktProgram-omfattningDeltid')),
        };

        this.funktionsnedsattning = element(by.id('funktionsnedsattning'));

        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

        this.harForhinder = element(by.id('harForhinder'));

        this.sjukfranvaro = {
            getRow: function (index) {
                return {
                    niva: element(by.id('sjukfranvaro-row' + index + '-col0')),
                    from: element(by.id('sjukfranvaro-row' + index + '-col1')),
                    tom: element(by.id('sjukfranvaro-row' + index + '-col2'))
                }
            }
        };

        this.begransningSjukfranvaro = {
            kanBegransas: element(by.id('begransningSjukfranvaro-kanBegransas')),
            text: element(by.id('begransningSjukfranvaro-beskrivning'))
        };

        this.prognosAtergang = {
            prognos: element(by.id('prognosAtergang-prognos')),
            text: element(by.id('prognosAtergang-anpassningar'))
        }
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verify: function (data) {
        this.verifieraMinUndersokning(data.minUndersokning);
        this.verifieraAnnanUndersokning(data.annanUndersokning);
        this.verifieraArbetsmarknadspolitisktProgram(data.arbetsmarknadspolitisktProgram);
        this.verifieraFunktionsnedsattning(data.funktionsNedsattning);
        this.verifieraAktivitetsbegransning(data.aktivitetsBegransning);
        this.verifieraHarForhinder(data.harForhinder);
        this.verifieraSjukfranvaro(data.sjukfranvaro);
        this.verifieraBegrasningSjukfranvaro(data.begransningSjukfranvaro);
        this.verifieraPrognosAtergang(data.prognosAtergang);
    },
    verifieraMinUndersokning: function (undersokning) {
        if (undersokning.checked) {
            if (undersokning.datum != undefined) {
                expect(this.minUndersokning.getText()).toBe(undersokning.datum);
            } else {
                expect(this.minUndersokning.getText()).toBe(testdataHelper.dateFormat(new Date()));
            }
        } else {
            expect(this.minUndersokning.getText()).toBe(EJ_ANGIVET);
        }
    },
    verifieraAnnanUndersokning: function (undersokning) {
        if (undersokning.checked) {
            if (undersokning.datum != undefined) {
                expect(this.annat.datum.getText()).toBe(undersokning.datum);
            } else {
                expect(this.annat.datum.getText()).toBe(testdataHelper.dateFormat(new Date()));
            }
            expect(this.annat.text.getText()).toBe(undersokning.text);
        } else {
            expect(this.annat.datum.getText()).toBe(EJ_ANGIVET);
            expect(this.annat.text.getText()).toBe(EJ_ANGIVET);
        }
    },
    verifieraArbetsmarknadspolitisktProgram: function (program) {
        expect(this.arbetsmarknadspolitisktProgram.text.getText()).toBe(program.text);

        expect(this.arbetsmarknadspolitisktProgram.omfattning.getAttribute("key"))
            .toBe("OMFATTNING.PROGRAM_" + program.radio + ".RBK");
        if (program.radio == 'DELTID') {
            expect(this.arbetsmarknadspolitisktProgram.deltidText.getText()).toBe(
                new Number(program.deltidText).toString());
        }
    },
    verifieraFunktionsnedsattning: function (nedsattning) {
        expect(this.funktionsnedsattning.getText())
            .toBe(testdataHelper.ejAngivetIfNull(nedsattning));
    },
    verifieraAktivitetsbegransning: function (begransning) {
        expect(this.aktivitetsbegransning.getText())
            .toBe(testdataHelper.ejAngivetIfNull(begransning));
    },
    verifieraHarForhinder: function (forhinder) {
        expect(this.harForhinder.getText()).toBe(testdataHelper.boolTillJaNej(forhinder));
    },
    verifieraSjukfranvaro: function (sjukfranvaro) {
        if (sjukfranvaro.length > 0) {
            var checkedRows = sjukfranvaro.filter(row => row.checked);

            for (var i=0; i<checkedRows.length; i++) {
                var pageRow = this.sjukfranvaro.getRow(i);
                expect(pageRow.niva.getText()).toBe(new Number(checkedRows[i].niva).toString());
                expect(pageRow.from.getText()).toBe(checkedRows[i].from);
                expect(pageRow.tom.getText()).toBe(checkedRows[i].tom);
            }
        }
    },
    verifieraBegrasningSjukfranvaro: function (begransning) {
        expect(this.begransningSjukfranvaro.kanBegransas.getText())
            .toBe(testdataHelper.boolTillJaNej(begransning.value));
        if (begransning.value) {
            expect(this.begransningSjukfranvaro.text.getText())
                .toBe(testdataHelper.ejAngivetIfNull(begransning.text));
        } else {
            expect(this.begransningSjukfranvaro.text.getText()).toBe(EJ_ANGIVET);
        }
    },
    verifieraPrognosAtergang: function (prognos) {
        expect(this.prognosAtergang.prognos.getAttribute("key"))
            .toBe("PROGNOS_ATERGANG." + prognos.radio + ".RBK");
        if (prognos.radio == "ATERGA_MED_ANPASSNING") {
            expect(this.prognosAtergang.text.getText())
                .toBe(prognos.text);
        } else {
            expect(this.prognosAtergang.text.getText())
                .toBe(EJ_ANGIVET);
        }
    }
});

module.exports = new Af00251Intyg();
