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

/* globals pages, protractor, logg */
'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
var basePage = pages.webcertBase;
// var intygPage = pages.intyg.base.intyg;
var utkastPage = pages.intyg.base.utkast;
var unsignedPage = pages.unsignedPage;
// var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;

module.exports = function () {

  this.Given(/^går in på Sök\/skriv intyg$/, function (callback) {
    basePage.flikar.sokSkrivIntyg.click().then(callback);
  });

  this.Given(/^går in på Ej signerade utkast$/, function (callback) {
    unsignedPage.flikar.notSigned.click().then(callback);
  });

  this.Given(/^är kopieraknappen tillgänglig$/, function (callback) {
    expect(basePage.copyBtn.isPresent()).to.become(true).then(function () {
      logg('OK - Kopiera knappen hittad');
      basePage.copyBtn.sendKeys(protractor.Key.SPACE).then(callback);
    }, function (reason) {
      callback('FEL : ' + reason);
    }).then(callback);
  });

  this.Given(/^är kopieraknappen inte tillgänglig$/, function (callback) {
    expect(basePage.copyBtn.isPresent()).to.become(false).then(function () {
      logg('OK - Kopiera knappen syns inte');
    }, function (reason) {
      callback('FEL : ' + reason);
    }).then(callback);
  });

  this.Given(/^synns Vidarebefodra knappen$/, function (callback) {
    expect(basePage.copyBtn.isPresent()).to.become(true).then(function () {
      logg('OK - Vidarebeforda knappen hittad');
    }, function (reason) {
      callback('FEL : ' + reason);
    }).then(callback);
  });

  this.Given(/^väljer att visa sökfilter/, function (callback) {
    unsignedPage.showSearchFilters().then(callback);
  });

  this.Given(/^ska sökfiltret Sparat av inte vara tillgängligt/, function (callback) {
    expect(unsignedPage.filterSavedBy.form.isPresent()).to.eventually.be.not.ok.then(function (value) {
      logg('Filter \"Sparat av\" inte tillgängligt för uthoppsläkare ' + value);
    }, function (reason) {
      callback('FEL, Filter \"Sparat av\" tillgängligt för uthoppsläkare,' + reason);
    }).then(callback);
  });

  this.Given(/^avbryter jag vidarebefodran$/, function (callback) {
    element(by.id('buttonNo')).sendKeys(protractor.Key.SPACE).then(callback);
  });

  this.Given(/^kopierar ett signerat intyg$/, function (callback) {
    expect(fkIntygPage.forwardBtn.isPresent()).to.become(true).then(function () {
      logg('OK - Kopiera knappen hittad');
    }, function (reason) {
      callback('FEL : ' + reason);
    }).then(callback);
  });

  this.Given(/^är signeraknappen tillgänglig$/, function (callback) {
    expect(utkastPage.signeraButton.isPresent()).to.eventually.be.ok.then(function (value) {
      logg('Signeringsknapp existerar ' + value);
    }, function (reason) {
      callback('FEL, Signeringsknapp finns inte på sidan,' + reason);
    });

    expect(utkastPage.signeraButton.isEnabled()).to.eventually.be.ok.then(function (value) {
      logg('Signeringsknapp är klickbar' + value);
    }, function (reason) {
      callback('FEL, Signeringsknapp är inte klickbar,' + reason);
    }).then(callback);
  });

  this.Given(/^ska makuleraknappen inte vara tillgänglig$/, function (callback) {
    expect(fkIntygPage.makulera.btn.isPresent()).to.eventually.be.not.ok.then(function (value) {
      logg('Makuleraknappen syns inte (ok)' + value);
    }, function (reason) {
      callback('FEL, Makuleraknappen finns tillgänglig,' + reason);
    }).then(callback);
  });

  this.Given(/^väljer att byta vårdenhet$/, function (callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
  });

  this.Given(/^vårdenhet ska vara "([^"]*)"$/, function (arg1, callback) {
    expect(basePage.careUnit.getText()).to.eventually.contain(arg1).then(function (value) {
      logg('OK - vårdenhet = ' + value);
    }, function (reason) {
      callback('FEL - vårdenhet: ' + reason);
    }).then(callback);
  });

  this.Given(/^jag väljer flik "([^"]*)"$/, function (arg1, callback) {
    expect(basePage.flikar.sokSkrivIntyg.getText()).to.eventually.contain(arg1).then(function (value) {
      element(basePage.flikar.sokSkrivIntyg).sendKeys(protractor.Key.SPACE);
      logg('OK - byta flik till = ' + value);
    }, function (reason) {
      callback('FEL - byta flik till: ' + reason);
    }).then(callback);
  });

  this.Given(/^jag väljer att byta vårdenhet$/, function (callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(callback);
  });

  this.Given(/^väljer "([^"]*)"$/, function (arg1, callback) {
    basePage.changeUnit.sendKeys(protractor.Key.SPACE).then(function (arg1) {
      element(by.id('select-active-unit-IFV1239877878-1045-modal')).sendKeys(protractor.Key.SPACE).then(callback);
    });
  });

  this.Given(/^synns inte signera knappen$/, function (callback) {
    fkUtkastPage.signeraButton.isPresent().then(function (isVisible) {
      if (isVisible) {
        callback('FEL - Signera knapp synlig!');
      } else {
        console.log('OK - Signera knapp ej synlig!');
      }
    }).then(callback);
  });

  this.Given(/^synns Hämta personuppgifter knappen$/, function (callback) {
    fkUtkastPage.fetchPatientButton.isPresent().then(function (isVisible) {
      if (isVisible) {
        console.log('OK - Hämta personuppgifter synlig!');
      } else {
        callback('FEL - Hämta personuppgifter ej synlig!');
      }
    }).then(callback);
  });

  this.Given(/^meddelas jag om spärren$/, function (callback) {
    expect(basePage.warnings.protectedInfo).getText()
      .to.eventually.contain('På grund av sekretessmarkeringen går det inte att skriva nya elektroniska intyg.').then(function (value) {
        logg('OK - sekretessmarkeringe = ' + value);
      }, function (reason) {
        callback('FEL - sekretessmarkeringe: ' + reason);
      }).then(callback);

  });

  this.Given(/^jag kan inte gå in på att skapa ett "([^"]*)" intyg$/, function (arg1, callback) {
    sokSkrivIntygUtkastTypePage.intygTypeButton.isDisplayed().then(function (isVisible) {
      if (isVisible) {
        callback('FEL - ' + arg1 + ' synlig!');
      } else {
        console.log('OK -' + arg1 + ' ej synlig!');
      }
    }).then(callback);
  });

  // ============== PLACEHOLDER TO PAUSE TESTS =================
  this.Given(/^jag väntar$/, function (callback) {
    // Write code here that turns the phrase above into concrete actions
    // callback.pending();
  });
  // ===========================================================

  this.Given(/^jag svarar på "([^"]*)" på frågan$/, function (arg1, callback) {
    fkIntygPage.answer.text.sendKeys(arg1).then(function () {
      basePage.QnA.respond.sendKeys(protractor.Key.SPACE).then(callback);
    });
  });

  this.Given(/^ska frågan vara hanterad$/, function (callback) {
    callback.pending();
  });

};
