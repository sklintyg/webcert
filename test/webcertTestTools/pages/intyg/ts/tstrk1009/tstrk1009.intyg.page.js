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

/**
 * Created by bennysce on 09/06/15.
 */
'use strict';

var TsBaseIntyg = require('../ts.base.intyg.page');
var testValues = require('../../../../testdata/testvalues.ts.js');
var _ = require('lodash');

var TsTrk1009Intyg = TsBaseIntyg._extend({
  init: function init() {
    init._super.call(this);
    this.intygType = 'tstrk1009';
    this.intygTypeVersion = '1.0';

    this.identitetStyrktGenom = element(by.id('identitetStyrktGenom-typ'));
    this.anmalanAvser = element(by.id('anmalanAvser-typ'));
    this.medicinskaForhallanden = element(by.id('medicinskaForhallanden'));
    this.senasteUndersokningsdatum = element(by.id('senasteUndersokningsdatum'));
    this.intygetAvserBehorigheter = element(by.id('intygetAvserBehorigheter-typer-0'));
    this.informationOmTsBeslutOnskas = element(by.id('informationOmTsBeslutOnskas'));
  },
  get: function get(intygId) {
    get._super.call(this, intygId);
  },
  verify: function(data) {
    expect(this.identitetStyrktGenom.getText()).toBe(data.identitetStyrktGenom);
    expect(this.anmalanAvser.getText()).toBe(
        data.anmalanAvser == 'OLAMPLIGHET' ? 'Anmälan om olämplighet' : 'Anmälan om sannolik olämplighet');
    expect(this.medicinskaForhallanden.getText()).toBe(data.medicinskaForhallanden);
    expect(this.senasteUndersokningsdatum.getText()).toBe(data.senasteUndersokningsdatum);
    expect(this.intygetAvserBehorigheter.getText()).toBe(data.intygetAvserBehorigheter[0]);
    expect(this.informationOmTsBeslutOnskas.getText()).toBe(data.informationOmTsBeslutOnskas ? 'Ja' : 'Nej');
  }
});

module.exports = new TsTrk1009Intyg();
