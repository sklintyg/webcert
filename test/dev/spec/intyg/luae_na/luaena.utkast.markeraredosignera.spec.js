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

/*globals afterAll,beforeAll,describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.luaeNA.utkast;
var restUtil = wcTestTools.restUtil;

describe('Create partially complete luae_na utkast and mark as ready to sign', function() {

  var utkastId = null,
      data = null;

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    restUtil.registerEnhetAsDjupintegrerad('TSTNMT2321000156-1039', 'Vårdenhetens namn', 'vgid', 'vgnamn', false,
        true);
    specHelper.login();
    specHelper.createUtkastForPatient('191212121212', 'luae_na');
  });

  describe('Skapa luae_na', function() {

    describe('Fyll i utkastet', function() {

      it('Spara undan intygsId från URL', function() {

        specHelper.getUtkastIdFromUrl().then(function(id) {
          utkastId = id;
        });
        data = wcTestTools.testdata.fk.LUAE_NA.get(utkastId);
      });
    });

    describe('Byt till djupintegrerad vårdadministratör på utkastet och markera som klar för signering', function() {

      it('Bli vårdadmin och djupintegrerad, ladda sedan om sidan', function() {
        browser.ignoreSynchronization = true;
        specHelper.setUserRole("VARDADMINISTRATOR").then(function() {
          specHelper.setUserOrigin("DJUPINTEGRATION").then(function() {
            browser.ignoreSynchronization = false;
            UtkastPage.get(utkastId);
          });
        });
      });

      it('Verifiera att knappen för Markera som klart att signera syns', function() {
        expect(UtkastPage.isMarkeraSomKlartAttSigneraButtonDisplayed()).toBeTruthy();
      });

      it('Klicka på knappen för Markera som klart att signera syns', function() {
        UtkastPage.markeraSomKlartAttSigneraButtonClick();

        // Vänta på att den modala dialogen öppnas och Yes-knappen blir synlig
        expect(UtkastPage.markeraKlartForSigneringModalYesButton.isDisplayed()).toBeTruthy();
      });

      it('Klicka knappen för Markera utkast redo att signera', function() {
        UtkastPage.markeraKlartForSigneringModalYesButton.sendKeys(protractor.Key.SPACE);

        expect(UtkastPage.markeradKlartForSigneringText.isDisplayed()).toBeTruthy();
      });
    });
  });

  afterAll(function() {
    testdataHelper.deleteUtkast(utkastId);
    restUtil.deregisterEnhetAsDjupintegrerad('TSTNMT2321000156-1039');
  });

});
