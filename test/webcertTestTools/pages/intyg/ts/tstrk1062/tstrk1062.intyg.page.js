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

var TsBaseIntyg = require('../ts.base.intyg.page');
var _ = require('lodash');

var Tstrk1062Intyg = TsBaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'tstrk1062';
        this.intygTypeVersion = '1.0';

        this.intygetAvser = {
            am: element(by.id('intygAvser-behorigheter-0')),
            a1: element(by.id('intygAvser-behorigheter-1'))
        };
        this.identitet = {
            svensktKortkort: element(by.id('idKontroll-typ'))
        };
        this.allmant = {
            diagnosKod: element(by.id('diagnosKodad-row0-col0')),
            diagnosBeskrivning: element(by.id('diagnosKodad-row0-col1')),
            diagnosAr: element(by.id('diagnosKodad-row0-col2'))
        };
        this.lakemedelsbehandling = {
            harHaft: element(by.id('lakemedelsbehandling-harHaft')),
            pagar: element(by.id('lakemedelsbehandling-pagar')),
            aktuell: element(by.id('lakemedelsbehandling-aktuell')),
            pagatt: element(by.id('lakemedelsbehandling-pagatt')),
            effekt: element(by.id('lakemedelsbehandling-effekt')),
            foljsamhet: element(by.id('lakemedelsbehandling-foljsamhet'))
        };
        this.symptom = {
            bedomningAvSymptom: element(by.id('bedomningAvSymptom')),
            prognosTillstandGod: element(by.id('prognosTillstand-typ'))
        };
        this.ovrigt = element(by.id('ovrigaKommentarer'));
        this.bedomning = {
          am: element(by.id('bedomning-uppfyllerBehorighetskrav-0')),
          a: element(by.id('bedomning-uppfyllerBehorighetskrav-1'))
        };
    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
    verify: function(data) {
        expect(this.intygetAvser.am.getText()).toBe(data.intygetAvser.am);
        expect(this.intygetAvser.a1.getText()).toBe(data.intygetAvser.a1);
        expect(this.identitet.svensktKortkort.getText()).toBe(data.identitet.svensktKortkort);
        expect(this.allmant.diagnosKod.getText()).toBe(data.allmant.diagnosKod);
        expect(this.allmant.diagnosBeskrivning.getText()).toBe(data.allmant.diagnosBeskrivning);
        expect(this.allmant.diagnosAr.getText()).toBe(data.allmant.diagnosAr);
        expect(this.lakemedelsbehandling.harHaft.getText()).toBe(data.lakemedelsbehandling.harHaft);
        expect(this.lakemedelsbehandling.pagar.getText()).toBe(data.lakemedelsbehandling.pagar);
        expect(this.lakemedelsbehandling.aktuell.getText()).toBe(data.lakemedelsbehandling.aktuell);
        expect(this.lakemedelsbehandling.pagatt.getText()).toBe(data.lakemedelsbehandling.pagatt);
        expect(this.lakemedelsbehandling.effekt.getText()).toBe(data.lakemedelsbehandling.effekt);
        expect(this.lakemedelsbehandling.foljsamhet.getText()).toBe(data.lakemedelsbehandling.foljsamhet);
        expect(this.symptom.bedomningAvSymptom.getText()).toBe(data.symptom.bedomningAvSymptom);
        expect(this.symptom.prognosTillstandGod.getText()).toBe(data.symptom.prognosTillstandGod);
        expect(this.ovrigt.getText()).toBe(data.ovrigt);
        expect(this.bedomning.am.getText()).toBe(data.bedomning.am);
        expect(this.bedomning.a.getText()).toBe(data.bedomning.a);
    }
});

module.exports = new Tstrk1062Intyg();
