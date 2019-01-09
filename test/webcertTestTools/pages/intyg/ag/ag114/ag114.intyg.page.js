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

'use strict';

var AgBaseIntyg = require('../ag.base.intyg.page.js');

var Ag114Intyg = AgBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ag114';
        this.intygTypeVersion = '1.0';

        this.sysselsattning = {
            text: element(by.id('nuvarandeArbete'))
        };
        this.onskarFormedlaDiagnos = {
            value: element(by.id('onskarFormedlaDiagnos')),
            diagnosRow: function(index) {
                return {
                    kod: element(by.id('diagnoser-row' + index + '-col0')),
                    beskrivning: element(by.id('diagnoser-row' + index + '-col1'))
                };
            },
        };
        this.nedsattArbetsformaga = {
            text: element(by.id('nedsattArbetsformaga')),
            yes: element(by.id('arbetsformagaTrotsSjukdom')),
            formaga: element(by.id('arbetsformagaTrotsSjukdomBeskrivning'))
        };
        this.bedomning = {
            sjukskrivningsgrad: element(by.id('sjukskrivningsgrad')),
            period: element(by.id('sjukskrivningsperiod'))
        };
        this.ovrigt = element(by.id('ovrigaUpplysningar'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verify: function(data) {
        this.verifieraSysselsattning(data);
        this.verifieraOnskarFormedlaDiagnos(data);
        this.verifieraNedsattArbetsformaga(data);
        this.verifieraBedomning(data);
        this.verifieraOvrigt(data);
    },
    verifieraSysselsattning: function(data) {
        if (data.sysselsattning.text !== undefined) {
            expect(this.sysselsattning.text.getText()).toBe(data.sysselsattning.text);
        }
    },
    verifieraOnskarFormedlaDiagnos: function(data) {
        if (data.onskarFormedlaDiagnos.value !== undefined) {
            expect(this.onskarFormedlaDiagnos.value.getText()).toBe(data.onskarFormedlaDiagnos.yes);
        }
        if (data.onskarFormedlaDiagnos.diagnoser !== undefined) {
            for (var i = 0; i < data.onskarFormedlaDiagnos.diagnoser.length; i++) {
                expect(this.onskarFormedlaDiagnos.diagnosRow(i).kod.getText()).toBe(data.onskarFormedlaDiagnos.diagnoser[i]);
            }
        }
    },
    verifieraNedsattArbetsformaga: function(data) {
        if (data.nedsattArbetsformaga.yes !== undefined) {
            expect(this.nedsattArbetsformaga.yes.getText()).toBe(data.nedsattArbetsformaga.yes);
        }
        if (data.nedsattArbetsformaga.text !== undefined) {
            expect(this.nedsattArbetsformaga.text.getText()).toBe(data.nedsattArbetsformaga.text);
        }
        if (data.nedsattArbetsformaga.formaga !== undefined) {
            expect(this.nedsattArbetsformaga.formaga.getText()).toBe(data.nedsattArbetsformaga.formaga);
        }
    },
    verifieraBedomning: function(data) {
        if (data.bedomning.sjukskrivningsgrad !== undefined) {
            expect(this.bedomning.sjukskrivningsgrad.getText()).toBe(data.bedomning.sjukskrivningsgrad);
        }
        if (data.bedomning.period !== undefined) {
            expect(this.bedomning.period.getText()).toBe('Fr.o.m ' + data.bedomning.from + ' t.o.m ' + data.bedomning.tom);
        }
    },
    verifieraOvrigt: function(data) {
        expect(this.ovrigt.getText()).toBe(data.ovrigt);
    }
});

module.exports = new Ag114Intyg();
