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
/*globals describe,it,browser */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.ts.diabetes.v3.utkast;
var IntygPage = wcTestTools.pages.intyg.ts.diabetes.v3.intyg;

describe('Create and Sign ts-diabetes v3 utkast', function() {

  var utkastId = null,
      data = null;

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
    specHelper.createUtkastForPatient('191212121212', 'ts-diabetes');
  });

  it('Spara undan intygsId från URL', function() {
    UtkastPage.disableAutosave();

    specHelper.getUtkastIdFromUrl().then(function(id) {
      utkastId = id;
    });
    data = wcTestTools.testdata.ts.diabetes.v3.get(utkastId);
  });

  describe('Fyll i intyget', function() {

    it('fillInKorkortstyper', function() {
      UtkastPage.fillInKorkortstyper(data.korkortstyper);
    });

    it('fillInIdentitetStyrktGenom', function() {
      UtkastPage.fillInIdentitetStyrktGenom(data.identitetStyrktGenom);
    });

    it('fillInAllmant', function() {
      UtkastPage.fillInAllmant(data.allmant);
    });

    it('fillInSynfunktion', function() {
      UtkastPage.fillInSynfunktion(data.synfunktion);
    });

    it('fillInBedomning', function() {
      UtkastPage.enableAutosave();
      UtkastPage.fillInBedomning(data.bedomning);
    });
  });

  it('Signera intyget', function() {
    UtkastPage.whenSigneraButtonIsEnabled();
    UtkastPage.signeraButtonClick();
    expect(IntygPage.isAt()).toBeTruthy();
  });

  it('Verifiera intyg', function() {
    IntygPage.verify(data);
  });

  afterAll(function() {
    testdataHelper.deleteIntyg(utkastId);
    testdataHelper.deleteUtkast(utkastId);
  });
});
