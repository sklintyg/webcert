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

/*globals logger, Promise, protractor, pages, wcTestTools, JSON, browser*/

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)

/*
 *	Stödlib och ramverk
 *
 */

const {
  Given, // jshint ignore:line
  When, // jshint ignore:line
  Then // jshint ignore:line
} = require('cucumber');

var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkValues = wcTestTools.testdata.values.fk;
var helpers = require('./helpers.js');

/*
 *	Test steg
 *
 */

When(/^jag förnyar intyget$/, function() {

  let fornyatIntyg = Object.create(this.intyg);

  let intyg = this.intyg;

  return fkIntygPage.fornyaBtn.sendKeys(protractor.Key.SPACE)
  .then(function() {
    return fkIntygPage.fornyaDialog.btn.sendKeys(protractor.Key.SPACE)
    .then(function() {
      return helpers.pageReloadDelay();
    })
    .then(function() {
      return browser.getCurrentUrl()
      .then(function(text) {

        logger.info('fornyatIntyg.id: ' + fornyatIntyg.id);
        intyg.id = text.split('/').slice(-2)[0];
        intyg.id = intyg.id.split('?')[0];

        logger.info('intyg.id: ' + intyg.id);
      });
    });
  });

});

Then(/^ska fält för Baserat på vara tomma$/, function(callback) {
  var baserasPa = fkUtkastPage.baserasPa;
  var minUndersokning = baserasPa.minUndersokning.datum;
  var minTelefonkontakt = baserasPa.minTelefonkontakt.datum;
  var journaluppgifter = baserasPa.journaluppgifter.datum;
  var annat = baserasPa.annat.datum;

  if (this.intyg.smittskydd) {
    logger.info('Eftersom intyget är ett smittskyddsintyg så kontrolleras att baserat-på fälten inte visas');
    Promise.all([
      expect(minUndersokning.isDisplayed()).to.become(false),
      expect(minTelefonkontakt.isDisplayed()).to.become(false),
      expect(journaluppgifter.isDisplayed()).to.become(false),
      expect(annat.isDisplayed()).to.become(false)
    ]).then(function(value) {
      logger.info('OK - ' + value);
      callback();
    }, function(reason) {
      callback('FEL, ' + reason);
    });
  } else {
    Promise.all([
      expect(minUndersokning.getAttribute('value')).to.eventually.contain(''),
      expect(minTelefonkontakt.getAttribute('value')).to.eventually.contain(''),
      expect(journaluppgifter.getAttribute('value')).to.eventually.contain(''),
      expect(annat.getAttribute('value')).to.eventually.contain('')
    ]).then(function(value) {
      logger.info('OK - ' + value);
      callback();
    }, function(reason) {
      callback('FEL, ' + reason);
    });
  }

});
Then(/^ska fält för Bedömning av arbetsförmåga vara tomma$/, function(callback) {
  var nedsatt = fkUtkastPage.nedsatt;

  Promise.all([
    expect(nedsatt.med25.from.getAttribute('value')).to.eventually.contain(''),
    expect(nedsatt.med25.tom.getAttribute('value')).to.eventually.contain(''),

    expect(nedsatt.med50.from.getAttribute('value')).to.eventually.contain(''),
    expect(nedsatt.med50.tom.getAttribute('value')).to.eventually.contain(''),

    expect(nedsatt.med75.from.getAttribute('value')).to.eventually.contain(''),
    expect(nedsatt.med75.tom.getAttribute('value')).to.eventually.contain(''),

    expect(nedsatt.med100.from.getAttribute('value')).to.eventually.contain(''),
    expect(nedsatt.med100.tom.getAttribute('value')).to.eventually.contain('')

  ]).then(function(value) {
    logger.info('OK - ' + value);
    callback();
  }, function(reason) {
    callback('FEL, ' + reason);
  });
});

When(/^jag anger datum för Baserat på$/, function(callback) {
  this.intyg.baserasPa = fkValues.getRandomBaserasPa(this.intyg.smittskydd);
  //Ange baseras på
  fkUtkastPage.angeIntygetBaserasPa(this.intyg.baserasPa).then(function() {
    logger.info('OK - angeIntygetBaserasPa :' + JSON.stringify(this.intyg.baserasPa));
    callback();
  }, function(reason) {
    callback('FEL, angeIntygetBaserasPa,' + reason);
  });
});

When(/^jag anger datum för arbetsförmåga$/, function(callback) {
  this.intyg.arbetsformaga = fkValues.getRandomArbetsformaga();

  fkUtkastPage.angeArbetsformaga(this.intyg.arbetsformaga).then(function() {
    logger.info('OK - angeArbetsformaga :' + JSON.stringify(this.intyg.arbetsformaga));
    callback();
  }, function(reason) {
    callback('FEL, angeArbetsformaga,' + reason);
  });
});

Then(/^ska fält för Kontakt med FK vara tom$/, function(callback) {
  // Write code here that turns the phrase above into concrete actions
  expect(fkUtkastPage.kontaktFk.getAttribute('checked')).to.eventually.be.a('null')
  .then(function(value) {
    logger.info('OK - ' + value);
    callback();
  }, function(reason) {
    callback('FEL, ' + reason);
  });
});

When(/^jag anger kontakt med FK$/, function(callback) {
  this.intyg.kontaktOnskasMedFK = fkValues.getRandomKontaktOnskasMedFK();
  fkUtkastPage.angeKontaktOnskasMedFK(this.intyg.kontaktOnskasMedFK).then(function() {
    logger.info('OK - angeKontaktOnskasMedFK :' + JSON.stringify(this.intyg.kontaktOnskasMedFK));
    callback();
  }, function(reason) {
    callback('FEL, angeKontaktOnskasMedFK,' + reason);
  });
});
