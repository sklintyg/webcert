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

/**
 * Created by bennysce on 17-12-15.
 */
'use strict';

var JClass = require('jclass');

/**
 * Elements always shown in webcert are connected here. Header etc.
 */
var WebcertBasePage = JClass._extend({
  init: function () {
    this.doctor = element(by.css('.logged-in'));
    this.header = element(by.id('wcHeader'));

    this.flikar = {
      sokSkrivIntyg: element(by.id('menu-skrivintyg')),
      notSigned: element(by.id('menu-unsigned'))
    };
    this.copyBtn = element(by.id('copyBtn'));

    this.changeUnit = element(by.id('wc-care-unit-clinic-selector-link'));
    this.careUnit = element(by.css('.clearfix'));
    this.warnings = {
      protectedInfo: element(by.id('sekretessmarkering'))
    };

  },
  getDoctorText: function () {
    return this.doctor.getText();
  }
});

module.exports = WebcertBasePage;
