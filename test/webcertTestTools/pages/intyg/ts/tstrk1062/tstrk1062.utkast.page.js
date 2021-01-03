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

/*globals element,by,protractor, Promise,browser*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');

var Tstrk1062Utkast = BaseTsUtkast._extend({
  init: function init() {

    init._super.call(this);
    this.intygType = 'tstrk1062';
    this.intygTypeVersion = '1.0';
    this.at = element(by.id('edit-tstrk1062'));

    this.intygetAvser = {
      am: element(by.id('intygAvser.behorigheter-IAV11'))
    };
    this.identitet = {
      svensktKortkort: element(by.id('idKontroll.typ-KORKORT'))
    };
    this.allmant = {
      inmatningICD: element(by.id('diagnosRegistrering.typ-DIAGNOS_KODAD')),
      diagnosBeskrivning: element(by.id('diagnoseDescription-0')),
      diagnosAr: element(by.id('diagnosKodad-0--diagnosArtal'))
    };
    this.lakemedelsbehandling = {
      harHaftNo: element(by.id('lakemedelsbehandling-harHaftNo'))
    };
    this.symptom = {
      bedomningAvSymptom: element(by.id('bedomningAvSymptom')),
      prognosTillstandGodJa: element(by.id('prognosTillstand.typ-JA'))
    };
    this.bedomning = {
      am: element(by.id('bedomning.uppfyllerBehorighetskrav-VAR12'))
    };
  },
  fillIntygetAvser: function(utkastIntygetAvser) {
    var promiseArr = [];

    if (utkastIntygetAvser.am === 'AM') {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.intygetAvser.am, protractor.Key.SPACE));
    }
    return Promise.all(promiseArr);
  },
  fillIdentitet: function(utkastIdentitet) {
    var promiseArr = [];

    if (utkastIdentitet.svensktKortkort === 'Svenskt körkort') {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.identitet.svensktKortkort, protractor.Key.SPACE));
    }

    return Promise.all(promiseArr);
  },
  fillAllmant: function(utkastAllmant) {
    var promiseArr = [];

    if (utkastAllmant.inmatningICD === true) {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.allmant.inmatningICD, protractor.Key.SPACE));
    }
    promiseArr.push(pageHelpers.moveAndSendKeys(this.allmant.diagnosBeskrivning, utkastAllmant.diagnosBeskrivning)
    .then(browser.sleep(1000))
    .then(pageHelpers.moveAndSendKeys(this.allmant.diagnosBeskrivning, protractor.Key.ENTER)));
    promiseArr.push(pageHelpers.moveAndSendKeys(this.allmant.diagnosAr, utkastAllmant.diagnosAr));

    return Promise.all(promiseArr);
  },
  fillLakemedelsbehandling: function(utkastLakemedelsbehandling) {
    var promiseArr = [];

    if (utkastLakemedelsbehandling.harHaft === 'Nej') {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.harHaftNo, protractor.Key.SPACE));
    }

    return Promise.all(promiseArr);
  },
  fillSymptom: function(utkastSymptom) {
    var promiseArr = [];

    promiseArr.push(pageHelpers.moveAndSendKeys(this.symptom.bedomningAvSymptom, utkastSymptom.bedomningAvSymptom));
    if (utkastSymptom.prognosTillstandGod === 'Ja') {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.symptom.prognosTillstandGodJa, protractor.Key.SPACE));
    }

    return Promise.all(promiseArr);
  },
  fillBedomning: function(utkastBedomning) {
    var promiseArr = [];

    if (utkastBedomning.am === 'AM') {
      promiseArr.push(pageHelpers.moveAndSendKeys(this.bedomning.am, protractor.Key.SPACE));
    }
    return Promise.all(promiseArr);
  }
});

module.exports = new Tstrk1062Utkast();
