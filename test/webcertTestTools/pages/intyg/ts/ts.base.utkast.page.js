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
 * Created by bennysce on 09/06/15.
 */
/*globals element,by, protractor, Promise*/
'use strict';

var pageHelpers = require('../../pageHelper.util.js');
var BaseUtkast = require('../base.utkast.page.js');

var bedomning = {
  //form: element(by.id('form_bedomning')),
  form: [element(by.id('form_bedomning-korkortstyp')), element(by.id('form_bedomning'))],
  yes: element(by.id('bedomning-lamplighetInnehaBehorighetYes')),
  no: element(by.id('bedomning-lamplighetInnehaBehorighetNo'))
};

var BaseTsUtkast = BaseUtkast._extend({
  init: function init() {
    init._super.call(this);

    this.intygType = null; // overridden by children
    this.intygTypeVersion = null; // overridden by subclasses
    this.korkortsTyperChecks = element(by.id('form_intygAvser-korkortstyp')).all(by.css('label'));

    this.identitetForm = element(by.id('form_vardkontakt-idkontroll'));
    this.specialist = element(by.id('bedomning-lakareSpecialKompetens'));

    this.bedomning = bedomning;

    this.bedomningKorkortsTyperChecks = this.bedomning.form[0].all(by.css('label'));
    this.bedomningKorkortsTyperChecksDiabetes = this.bedomning.form[1].all(by.css('label'));

    this.kommentar = element(by.id('kommentar'));
    this.adress = this.patientAdress;

    this.markeraKlartForSigneringButton = element(by.id('markeraKlartForSigneringButton'));
    this.markeraKlartForSigneringModalYesButton = element(by.id('buttonYes'));
    this.markeradKlartForSigneringText = element(by.id('draft-marked-ready-text'));

  },
  get: function get(intygId) {
    get._super.call(this, this.intygType, intygId);
  },
  fillInKorkortstyper: function(typer) {
    return pageHelpers.selectAllCheckBoxes(this.korkortsTyperChecks, typer);
  },
  fillInIdentitetStyrktGenom: function(idtyp) {
    return this.identitetForm.element(by.cssContainingText('label', idtyp)).click();
  },
  fillInBedomningLamplighet: function(lamplighet) {
    if (lamplighet) {
      if (lamplighet === 'Ja') {
        return bedomning.yes.sendKeys(protractor.Key.SPACE);
      } else {
        return bedomning.no.sendKeys(protractor.Key.SPACE);
      }
    }
    return Promise.resolve('Inget svar på lämplighet angivet');
  },
  fillInBedomning: function(bedomningObj) {
    var fillInLamplighet = this.fillInBedomningLamplighet;
    var bedomningKorkortsTyperChecks = this.bedomningKorkortsTyperChecks;
    var bedomningKorkortsTyperChecksDiabetes = this.bedomningKorkortsTyperChecksDiabetes;

    if ('ts-bas' === this.intygType) {
      return pageHelpers.selectAllCheckBoxes(bedomningKorkortsTyperChecks, [bedomningObj.stallningstagande])
      .then(function() {
        return fillInLamplighet(bedomningObj.lamplighet);
      });
    } else if ('ts-diabetes' === this.intygType) {
      return element(by.cssContainingText('label', bedomningObj.stallningstagande)).click()
      .then(function() {
        return pageHelpers.selectAllCheckBoxes(bedomningKorkortsTyperChecksDiabetes, bedomningObj.behorigheter)
        .then(function() {
          return fillInLamplighet(bedomningObj.lamplighet);
        });
      });
    }
  },
  fillInOvrigKommentar: function(utkast) {
    return this.kommentar.sendKeys(utkast.kommentar);
  },
  fillInSpecialist: function(specialist) {
    return this.specialist.sendKeys(specialist);
  },
  isMarkeraSomKlartAttSigneraButtonDisplayed: function() {
    return this.markeraKlartForSigneringButton.isDisplayed();
  },
  markeraSomKlartAttSigneraButtonClick: function() {
    this.markeraKlartForSigneringButton.sendKeys(protractor.Key.SPACE);
  }
});

module.exports = BaseTsUtkast;
