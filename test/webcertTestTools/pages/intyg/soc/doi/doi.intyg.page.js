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

var BaseSocIntygPage = require('../soc.base.intyg.page.js');

var DoiIntyg = BaseSocIntygPage._extend({
    init: function init() {
        init._super.call(this);
        this.intygTypeVersion = '1.0';

        this.identitetStyrkt = element(by.id('identitetStyrkt'));
        this.dodsdatum = {
            dodsdatumSakert: element(by.id('dodsdatumSakert')),
            datum: element(by.id('dodsdatum')),
            antraffatDodDatum: element(by.id('antraffatDodDatum'))
        };
        this.dodsPlats = {
            kommun: element(by.id('dodsplatsKommun')),
            boende: element(by.id('dodsplatsBoende'))
        };
        this.barn = {
            value: element(by.id('barn'))
        };
        this.terminalDodsorsak = {
            dodsorsakbeskrivning:  element(by.id('terminalDodsorsak-row0-col0')),
            dodsorsakDatum: element(by.id('terminalDodsorsak-row0-col1')),
            dodsorsakTillstand: element(by.id('terminalDodsorsak-row0-col2'))
        };

        this.skadaForgiftning = {
            val: element(by.id('forgiftning')),
            orsak: element(by.id('forgiftningOrsak')),
            datum: element(by.id('forgiftningDatum')),
            beskrivning: element(by.id('forgiftningUppkommelse'))
        };
        this.operation = {
            val: element(by.id('operation')),
            datum: element(by.id('operationDatum')),
            beskrivning: element(by.id('operationAnledning'))
        };
        this.grunderLista = element(by.id('grunder-list'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verifyDodsdatum: function(dodsdatum) {
       if (dodsdatum.sakert) {
            expect(this.dodsdatum.datum.getText()).toBe(dodsdatum.sakert.datum);
        }
    },
    verifyBarn: function(barn) {
        if(barn) {
            expect(this.barn.value.getText()).toBe('Ja');
        } else {
            expect(this.barn.value.getText()).toBe('Nej');
        }
    },
    verifyTerminalDodsorsak: function(dodsorsak) {
        expect(this.terminalDodsorsak.dodsorsakbeskrivning.getText()).toBe(dodsorsak.a.beskrivning);
        expect(this.terminalDodsorsak.dodsorsakDatum.getText()).toBe(dodsorsak.a.datum);
        expect(this.terminalDodsorsak.dodsorsakTillstand.getText()).toBe(dodsorsak.a.tillstandSpec);
    },
    verifyOperation: function(operation) {
        if(operation === 'Ja'){
            expect(this.operation.datum.getText()).toBe(operation.datum);
            expect(this.operation.beskrivning.getText()).toBe(operation.beskrivning);
        }
    },
    verifySkadaForgiftning: function(forgiftning) {
        if(forgiftning.val === 'Ja') {
            expect(this.skadaForgiftning.orsak.getText()).toBe(forgiftning.orsakAvsikt);
            expect(this.skadaForgiftning.datum.getText()).toBe(forgiftning.datum);
            expect(this.skadaForgiftning.beskrivning.getText()).toBe(forgiftning.beskrivning);
        }
    },
    verifyGrunder: function(grunderLista) {
        for (var i = 0; i < this.grunderLista.length; i++) {
            expect(this.grunderLista[i].getText()).toBe(grunderLista[i]);
        }
    },

    verify: function(data) {
        expect(this.identitetStyrkt.getText()).toBe(data.identitetStyrktGenom);
        this.verifyDodsdatum(data.dodsdatum);
        expect(this.dodsPlats.kommun.getText()).toBe(data.dodsPlats.kommun);
        this.verifyBarn(data.barn);
        this.verifyTerminalDodsorsak(data.dodsorsak);
        this.verifyOperation(data.operation);
        this.verifySkadaForgiftning(data.skadaForgiftning);
        this.verifyGrunder(data.dodsorsaksuppgifter);
    }
});
module.exports = new DoiIntyg();
