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

/*globals afterAll,beforeAll,describe,it,browser,protractor */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luaeNA.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('Validera makulering av luae_na Intyg', function() {

  var intyg = intygFromJsonFactory.defaultLuaena();
  var intygsId = intyg.id;

  beforeAll(function() {
    testdataHelper.deleteIntyg(intygsId);
    browser.ignoreSynchronization = false;
    specHelper.login();
  });

  describe('Visa signerat luae_na intyg', function() {

    it('Skapa signerat intyg i IT, visa intyget', function() {
      restUtil.createIntyg(intyg);
      SokSkrivIntygPage.selectPersonnummer('19121212-1212');
      SokSkrivValjIntyg.selectIntygById(intygsId);

      expect(IntygPage.isAt()).toBeTruthy();
      expect(IntygPage.makulera.statusRevoked.isPresent()).toBeFalsy();
    });

    it('Makulera intyget', function() {
      IntygPage.makulera.btn.sendKeys(protractor.Key.SPACE);
      IntygPage.makulera.dialogRadioAnnatAllvarligtFel.sendKeys(protractor.Key.SPACE);
      browser.wait(IntygPage.makulera.dialogRadioAnnatAllvarligtFelClarification.isDisplayed())
      .then(IntygPage.makulera.dialogRadioAnnatAllvarligtFelClarification.sendKeys(
          "Patienten har en helt annan diagnos än den angivna, blandade ihop mjältbrand med nageltrång. Lätt hänt..."));

      IntygPage.makulera.dialogMakulera.sendKeys(protractor.Key.SPACE);

      element.all(by.id('#makuleraBtn')).then(function(items) {
        expect(items.length).toBe(0);
      });

      expect(IntygPage.makulera.statusRevoked.isDisplayed()).toBeTruthy();

      // Print button should not exist
      expect(IntygPage.skrivUtBtn.isPresent()).toBeFalsy();
    });

  });

  afterAll(function() {
    testdataHelper.deleteIntyg(intygsId);
    specHelper.logout();
    browser.ignoreSynchronization = false;
  });

});
