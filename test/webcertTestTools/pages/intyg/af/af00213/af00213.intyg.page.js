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

var Af00213Intyg = AfBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'af00213';
        this.intygTypeVersion = '1.0';

        this.funktionsnedsattning = {
            value: element(by.id('harFunktionsnedsattning')),
            text: element(by.id('funktionsnedsattning'))
        };

        this.aktivitetsbegransning = {
            value: element(by.id('harAktivitetsbegransning')),
            text: element(by.id('aktivitetsbegransning'))
        };

        this.utredningBehandling = {
            value: element(by.id('harUtredningBehandling')),
            text: element(by.id('utredningBehandling'))
        };
        this.arbetetsPaverkan = {
            value: element(by.id('harArbetetsPaverkan')),
            text: element(by.id('arbetetsPaverkan'))
        };

        this.ovrigt = element(by.id('ovrigt'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verify: function(data) {
        this.verifieraFunktionsnedsattning(data);
        this.verifieraAktivitetsbegransning(data);
        this.verifieraUtredningBehandling(data);
        this.verifieraArbetetsPaverkan(data);
        this.verifieraOvrigt(data);
    },
    verifieraFunktionsnedsattning: function(data) {
        expect(this.funktionsnedsattning.value.getText()).toBe(data.funktionsnedsattning.val);
        if (data.funktionsnedsattning.text !== undefined) {
            expect(this.funktionsnedsattning.text.getText()).toBe(data.funktionsnedsattning.text);
        }
    },
    verifieraAktivitetsbegransning: function(data) {
        if (data.aktivitetsbegransning.val !== undefined) {
            expect(this.aktivitetsbegransning.value.getText()).toBe(data.aktivitetsbegransning.val);
            if (data.aktivitetsbegransning.text !== undefined) {
                expect(this.aktivitetsbegransning.text.getText()).toBe(data.aktivitetsbegransning.text);
            }
        }
    },
    verifieraUtredningBehandling: function(data) {
        expect(this.utredningBehandling.value.getText()).toBe(data.utredningBehandling.val);
        if (data.utredningBehandling.text !== undefined) {
            expect(this.utredningBehandling.text.getText()).toBe(data.utredningBehandling.text);
        }
    },
    verifieraArbetetsPaverkan: function(data) {
        expect(this.arbetetsPaverkan.value.getText()).toBe(data.arbetetsPaverkan.val);
        if (data.arbetetsPaverkan.text !== undefined) {
            expect(this.arbetetsPaverkan.text.getText()).toBe(data.arbetetsPaverkan.text);
        }
    },
    verifieraOvrigt: function(data) {
        expect(this.ovrigt.getText()).toBe(data.ovrigt);
    }
});

module.exports = new Af00213Intyg();
