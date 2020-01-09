/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

var BaseIntyg = require('../base.intyg.page.js');

var AgBaseIntyg = BaseIntyg._extend({
  init: function init() {
    init._super.call(this);
    this.baseratPa = {
      minUndersokningAvPatienten: element(by.id('undersokningAvPatienten')),
      journaluppgifter: element(by.id('journaluppgifter')),
      telefonkontakt: element(by.id('telefonkontaktMedPatienten')),
      annat: element(by.id('annatGrundForMU')),
      annatBeskrivning: element(by.id('annatGrundForMUBeskrivning'))
    };
  },

  verifieraBaseratPa: function(data) {
    if (data.baseratPa.minUndersokningAvPatienten) {
      expect(this.baseratPa.minUndersokningAvPatienten.getText()).toBe(data.baseratPa.minUndersokningAvPatienten);
    }

    if (data.baseratPa.journaluppgifter) {
      expect(this.baseratPa.journaluppgifter.getText()).toBe(data.baseratPa.journaluppgifter);
    }

    if (data.baseratPa.telefonkontakt) {
      expect(this.baseratPa.telefonkontakt.getText()).toBe(data.baseratPa.telefonkontakt);
    }

    if (data.baseratPa.annat) {
      expect(this.baseratPa.annat.getText()).toBe(data.baseratPa.annat);
    }

    if (data.baseratPa.annatBeskrivning) {
      expect(this.baseratPa.annatBeskrivning.getText()).toBe(data.baseratPa.annatBeskrivning);
    }

  }
});

module.exports = AgBaseIntyg;
