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

var BaseUtkast = require('../base.utkast.page.js');
var pageHelpers = require('../../pageHelper.util.js');

var AgBaseUtkast = BaseUtkast._extend({
  init: function init() {
    init._super.call(this);

    this.at = element(by.css('.edit-form'));

    this.baseratPa = {
      minUndersokningAvPatienten: {
        checkbox: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=checkbox]')),
        datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
      },
      telefonkontakt: {
        checkbox: element(by.id('form_telefonkontaktMedPatienten')).element(by.css('input[type=checkbox]')),
        datum: element(by.id('form_telefonkontaktMedPatienten')).element(by.css('input[type=text]'))
      },
      journaluppgifter: {
        checkbox: element(by.id('form_journaluppgifter')).element(by.css('input[type=checkbox]')),
        datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
      },
      annat: {
        beskrivning: element(by.id('annatGrundForMUBeskrivning')),
        checkbox: element(by.id('form_annatGrundForMU')).element(by.css('input[type=checkbox]')),
        datum: element(by.id('form_annatGrundForMU')).element(by.css('input[type=text]'))
      }
    };

  },

  angeBaseratPa: function(baseratPa) {

    var baseratPaElmObj = this.baseratPa;
    return new Promise(function(resolve) {
      resolve('anger BaseratPa');
    })
    .then(function() {
      if (baseratPa.minUndersokningAvPatienten) {
        return pageHelpers.moveAndSendKeys(baseratPaElmObj.minUndersokningAvPatienten.datum, baseratPa.minUndersokningAvPatienten);
      }
    })
    .then(function() {
      if (baseratPa.journaluppgifter) {
        return pageHelpers.moveAndSendKeys(baseratPaElmObj.journaluppgifter.datum, baseratPa.journaluppgifter);
      }
    })
    .then(function() {
      if (baseratPa.telefonkontakt) {
        return pageHelpers.moveAndSendKeys(baseratPaElmObj.telefonkontakt.datum, baseratPa.telefonkontakt);
      }
    })
    .then(function() {
      if (baseratPa.annat) {
        return pageHelpers.moveAndSendKeys(baseratPaElmObj.annat.datum, baseratPa.annat)
        .then(function() {
          return pageHelpers.moveAndSendKeys(baseratPaElmObj.annat.beskrivning, baseratPa.annatBeskrivning);
        });
      }
    });
  },

  get: function get(intygId) {
    get._super.call(this, this.intygType, intygId);
  }
});

module.exports = AgBaseUtkast;
