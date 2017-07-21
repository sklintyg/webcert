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



'use strict';

var BaseSmiIntygPage = require('../smi.base.intyg.page.js');
var testdataHelper = require('common-testtools').testdataHelper;

var LisjpIntyg = BaseSmiIntygPage._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'lisjp';
        this.funktionsnedsattning = element(by.id('funktionsnedsattning'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));

        this.sjukskrivningar = {
            grad: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col0'));
            },
            from: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col1'));
            },
            to: function(index) {
                return element(by.id('sjukskrivningar-row' + index + '-col2'));
            }
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        if (data.diagnos.diagnoser) {
            for (var j = 0; j < data.diagnos.diagnoser.length; j++) {
                expect(this.diagnoser.getDiagnos(j).kod.getText()).toBe(data.diagnos.diagnoser[j].kod);
            }
        }

        expect(this.ovrigt.getText()).toBe(data.ovrigt);

        this.verifyArbetsformaga(data.arbetsformaga);

        if (!data.smittskydd) {
            expect(this.baseratPa.minUndersokningAvPatienten.getText()).toBe(data.baseratPa.minUndersokningAvPatienten);
            expect(this.baseratPa.journaluppgifter.getText()).toBe(data.baseratPa.journaluppgifter);
            expect(this.baseratPa.telefonkontakt.getText()).toBe(data.baseratPa.telefonkontakt);
            expect(this.baseratPa.annat.getText()).toBe(data.baseratPa.annat);
            expect(this.baseratPa.annatBeskrivning.getText()).toBe(data.baseratPa.annatBeskrivning);

            expect(this.funktionsnedsattning.getText()).toBe(data.funktionsnedsattning);
            expect(this.aktivitetsbegransning.getText()).toBe(data.aktivitetsbegransning);

            expect(this.behandling.pagaende.getText()).toBe(data.medicinskbehandling.pagaende);
            expect(this.behandling.planerad.getText()).toBe(data.medicinskbehandling.planerad);

            this.kontaktFK.verify(data);

            if (data.tillaggsfragor) {
                for (var i = 0; i < data.tillaggsfragor.length; i++) {
                    var fraga = data.tillaggsfragor[i];
                    expect(this.tillaggsfragor.getFraga(fraga.id).getText()).toBe(fraga.svar);
                }
            }
        }
    },

    verifyArbetsformaga: function(arbetsformaga) {

        var formagor = [];

        if (arbetsformaga.nedsattMed100) {
            formagor.push(arbetsformaga.nedsattMed100);
        }

        if (arbetsformaga.nedsattMed75) {
            formagor.push(arbetsformaga.nedsattMed75);
        }

        if (arbetsformaga.nedsattMed50) {
            formagor.push(arbetsformaga.nedsattMed50);
        }

        if (arbetsformaga.nedsattMed25) {
            formagor.push(arbetsformaga.nedsattMed25);
        }

        for (var i = 0; i < formagor.length; i++) {
            expect(this.sjukskrivningar.from(i).getText()).toBe(formagor[i].from);
            expect(this.sjukskrivningar.to(i).getText()).toBe(formagor[i].tom);
        }
    }
});
module.exports = new LisjpIntyg();
