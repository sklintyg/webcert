/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
 * Created by stephenwhite on 09/06/15.
 */
/* globals browser */

'use strict';

var WebcertBasePage = require('../webcert.base.page.js');

var SokSkrivIntyg = WebcertBasePage._extend({
  init: function init() {
    init._super.call(this);

    this.at = element(by.id('skapa-valj-patient'));
    this.personnummer = element(by.id('pnr'));
    this.pnButton = element(by.id('skapapersonnummerfortsatt'));
    this.sekretessmarkering = element(by.id('sekretessmarkering'));
    this.puerror = element(by.id('puerror'));
    this.namnFortsatt = element(by.id('namnFortsatt'));
    this.patientNamn = element(by.id('patientNamn'));
    this.intygLista = element(by.id('intygLista'));
    this.sokSkrivIntygForm = element(by.css('form[name=certForm]'));
  },
  get: function() {
    browser.get('#/create/choose-intyg-type/default/index');
  },
  setPersonnummer: function(pn) {
    return this.personnummer.sendKeys(pn);
  },
  selectPersonnummer: function(pn) {
    var pnButton = this.pnButton;
    return this.setPersonnummer(pn).then(function() {
      return pnButton.click();
    });
  }
});

module.exports = new SokSkrivIntyg();
