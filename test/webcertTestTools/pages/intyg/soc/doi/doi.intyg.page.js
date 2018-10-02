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

var BaseSocIntygPage = require('../soc.base.intyg.page.js');

var DoiIntyg = BaseSocIntygPage._extend({
    init: function init() {
        init._super.call(this);
        this.intygTypeVersion = '1.0';
        this.barn = {
            value: element(by.id('barn'))
        };
        this.terminalDodsorsak = element(by.id('terminalDodsorsak'));
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
    }
});
module.exports = new DoiIntyg();
