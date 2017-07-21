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

var LuaefsIntyg = BaseSmiIntygPage._extend({
    init: function init() {
        init._super.call(this);

        this.intygType = 'luae_fs';

        this.funktionsnedsattning = {
            debut: element(by.id('funktionsnedsattningDebut')),
            paverkan: element(by.id('funktionsnedsattningPaverkan'))
        };
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verify: function(data) {

        expect(this.baseratPa.minUndersokningAvPatienten.getText()).toBe(data.baseratPa.minUndersokningAvPatienten);
        expect(this.baseratPa.journaluppgifter.getText()).toBe(data.baseratPa.journaluppgifter);
        expect(this.baseratPa.annat.getText()).toBe(data.baseratPa.annat);
        expect(this.baseratPa.annatBeskrivning.getText()).toBe(data.baseratPa.annatBeskrivning);
        expect(this.baseratPa.personligKannedom.getText()).toBe(data.baseratPa.personligKannedom);
        expect(this.baseratPa.anhorigsBeskrivning.getText()).toBe(data.baseratPa.anhorigsBeskrivning);

        if (data.diagnos.diagnoser) {
            for (var j = 0; j < data.diagnos.diagnoser.length; j++) {
                expect(this.diagnoser.getDiagnos(j).kod.getText()).toBe(data.diagnos.diagnoser[j].kod);
            }
        }

        if (data.andraMedicinskaUtredningar) {
            for (var i = 0; i < data.andraMedicinskaUtredningar.length; i++) {
                var utredningEL = this.andraMedicinskaUtredningar.getUtredning(i);
                var utredningDatum = data.andraMedicinskaUtredningar[i].datum;
                expect(utredningEL.typ.getText()).toBe(data.andraMedicinskaUtredningar[i].underlag);
                expect(utredningEL.datum.getText()).toBe(utredningDatum);
                expect(utredningEL.info.getText()).toBe(data.andraMedicinskaUtredningar[i].infoOmUtredningen);
            }
        }

        expect(this.funktionsnedsattning.debut.getText()).toBe(data.funktionsnedsattning.debut);
        expect(this.funktionsnedsattning.paverkan.getText()).toBe(data.funktionsnedsattning.paverkan);

        expect(this.ovrigt.getText()).toBe(data.ovrigt);

        this.kontaktFK.verify(data);
    }
});
module.exports = new LuaefsIntyg();
