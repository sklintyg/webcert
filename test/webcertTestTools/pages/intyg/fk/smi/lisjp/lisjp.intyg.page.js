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

var BaseSmiIntygPage = require('../smi.base.intyg.page.js');

var LisjpIntyg = BaseSmiIntygPage._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'lisjp';
        this.funktionsnedsattning = element(by.id('funktionsnedsattning'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.smittskydd = element(by.id('avstangningSmittskydd'));
        this.sysselsattning = {
            list: element(by.id('sysselsattning-list')),
            yrkesAktiviteter: element(by.id('nuvarandeArbete'))
        };
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
        this.arbetsformagaFMB = element(by.id('forsakringsmedicinsktBeslutsstod'));


        this.arbetstidsforlaggning = {
            val: element(by.id('arbetstidsforlaggning')),
            motivering: element(by.id('arbetstidsforlaggningMotivering'))
        };
        this.resorTillArbete = element(by.id('arbetsresor'));
        this.prognosForArbetsformaga = element(by.id('prognos-typ'));
        this.atgarder = element(by.id('arbetslivsinriktadeAtgarder'));
        this.atgarderBeskrivning = element(by.id('arbetslivsinriktadeAtgarderBeskrivning'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        this.verifieraDiagnos(data);

        this.verifieraOvrigt(data);

        this.verifyArbetsformaga(data.arbetsformaga);

        if (!data.smittskydd) {
            this.verifieraBaseratPa(data);

            expect(this.funktionsnedsattning.getText()).toBe(data.funktionsnedsattning);
            this.verifieraAktivitetsbegransning(data);

            this.verifieraMedicinskbehandling(data);

            this.verifieraKontaktFK(data);

            this.verifieraTillaggsfragor(data);
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
