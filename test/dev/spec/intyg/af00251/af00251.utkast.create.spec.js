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
var UtkastPage = wcTestTools.pages.intyg.af['af00251'].utkast;
var IntygPage = wcTestTools.pages.intyg.af['af00251'].intyg;

describe('Create and Sign af00251 utkast', function() {

  var utkastId = null,
      data = null;

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
    testdataHelper.deleteAllUtkast();
    testdataHelper.deleteAllIntyg();
    specHelper.createUtkastForPatient('191212121212', 'af00251');
  });

  it('Spara undan intygsId från URL', function() {
    UtkastPage.disableAutosave();

    specHelper.getUtkastIdFromUrl().then(function(id) {
      utkastId = id;
    });

    data = wcTestTools.testdata.af.af00251.get(utkastId);
  });

  describe('Skapa af00251', function() {

    describe('Fyll i intyget', function() {

      it('Ange medicinskt underlag, min undersökning', function() {
        UtkastPage.angeMinUndersokning(data.minUndersokning);
      });
      it('Ange medicinskt underlag, annat', function() {
        UtkastPage.angeAnnanUndersokning(data.annanUndersokning);
      });
      it('Ange arbetsmaknadspolitiskt program', function() {
        UtkastPage.angeArbetmarksnadsPolitisktProgram(data.arbetsmarknadspolitisktProgram);
      });
      it('Ange funktionsnedsättning', function() {
        UtkastPage.angeFunktionsNedsattning(data.funktionsNedsattning);
      });

      it('Ange aktivitetsbegräsning', function() {
        UtkastPage.angeAktivitetsBegransning(data.aktivitetsBegransning);
      });

      it('Ange har förhinder', function() {
        UtkastPage.angeHarForhinder(data.harForhinder);
      });

      it('Ange har sjukfranvaro', function() {
        UtkastPage.angeSjukfranvaro(data.sjukfranvaro);
      });
      it('Ange har begränsning av sjukfranvaro', function() {
        UtkastPage.angeBegransningSjukfranvaro(data.begransningSjukfranvaro);
      });
      it('Ange har prognos återgång', function() {
        UtkastPage.enableAutosave();
        UtkastPage.angePrognosAtergang(data.prognosAtergang);
      });

    });

    it('Signera intyget', function() {
      UtkastPage.whenSigneraButtonIsEnabled();
      UtkastPage.signeraButtonClick();
      expect(IntygPage.isAt()).toBeTruthy();
    });

    it('Wait until intyg in IT', function() {
      // Om intyget inte hunnit processas av IT så hämtas det från WC. Då är inte uppgifter flyttade till övriga
      // upplysningar ännu.
      // Vänta tills intyget tagits emot av IT. Ladda därefter om sidan så datan säkert kommer från IT.
      IntygPage.waitUntilIntygInIT(utkastId);
      IntygPage.get(utkastId);
    });

    it('Verifiera intyg', function() {
      IntygPage.verify(data);
    });
  });

  afterAll(function() {
    testdataHelper.deleteIntyg(utkastId);
    testdataHelper.deleteUtkast(utkastId);
  });

});
