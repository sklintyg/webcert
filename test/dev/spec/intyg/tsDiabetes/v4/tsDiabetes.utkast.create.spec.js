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
var UtkastPage = wcTestTools.pages.intyg.ts.diabetes.v4.utkast;
var IntygPage = wcTestTools.pages.intyg.ts.diabetes.v4.intyg;
var hasHogreKorkortsbehorigheter = require('webcert-testtools/pages/pageHelper.util').hasHogreKorkortsbehorigheter;

describe('Create and sign ts-diabetes v4 utkast', function() {

  var utkastId = null,
      data = null;

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
    specHelper.createUtkastForPatient('191212121212', 'ts-diabetes');
  });

  it('Save certificate id from url', function() {
    UtkastPage.disableAutosave();

    specHelper.getUtkastIdFromUrl().then(function(id) {
      utkastId = id;
    });
    data = wcTestTools.testdata.ts.diabetes.v4.get(utkastId);
  });

  describe('Fill draft sections', function() {

    it('Fill IntygetAvser', function() {
      UtkastPage.fillIntygetAvser(data.intygetAvserKategorier);
    });

    it('Fill IdentitetStyrktGenom', function() {
      UtkastPage.fillIdentitetStyrktGenom(data.identitetStyrktGenom);
    });

    it('Fill Allmant', function() {
      UtkastPage.fillAllmant(data.allmant);
    });

    it('Fill Hypoglykemi', function() {
      if (data.allmant.medicinering === 'Ja' && data.allmant.medicineringHypoglykemi === 'Ja') {
        UtkastPage.fillHypoglykemiForMedication(data.hypoglykemi);
      }
      if (hasHogreKorkortsbehorigheter(data.intygetAvserKategorier)) {
        UtkastPage.fillHypoglykemiForHogreBehorigheter(data.hypoglykemi);
      }
    });

    it('Fill Ovrigt', function() {
      UtkastPage.fillOvrigt(data.ovrigt);
    });

    it('Fill Bedomning', function() {
      UtkastPage.fillBedomning(data.bedomning);
    });
  });

  it('Sign draft', function() {
    UtkastPage.whenSigneraButtonIsEnabled();
    UtkastPage.signeraButtonClick();
    expect(IntygPage.isAt()).toBeTruthy();
  });

  it('Verify certificate', function() {
    IntygPage.verify(data);
  });

  afterAll(function() {
    testdataHelper.deleteIntyg(utkastId);
    testdataHelper.deleteUtkast(utkastId);
  });
});
