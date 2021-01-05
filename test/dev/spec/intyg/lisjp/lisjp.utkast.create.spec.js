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
var UtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
var IntygPage = wcTestTools.pages.intyg.lisjp.intyg;
var ValjIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Create and Sign lisjp utkast', function() {

  var utkastId = null,
      data = null;

  beforeAll(function() {
    browser.ignoreSynchronization = false;
    specHelper.login();
  });

  xdescribe('smittskydd', function() {
    beforeAll(function() {
      ValjIntygPage.get();
      specHelper.createUtkastForPatient('191212121212', 'lisjp');
    });

    describe('Fyll i intyget', function() {

      it('Spara undan intygsId från URL', function() {
        UtkastPage.disableAutosave();

        specHelper.getUtkastIdFromUrl().then(function(id) {
          utkastId = id;
        });
        data = wcTestTools.testdata.fk.LISJP.get(utkastId, true);
      });

      it('angeSmittskydd', function() {
        UtkastPage.angeSmittskydd(data.smittskydd);
      });
      it('angeArbetsformaga', function() {
        UtkastPage.angeArbetsformaga(data.arbetsformaga);
      });
      it('angeDiagnos', function() {
        UtkastPage.enableAutosave();
        UtkastPage.angeDiagnos(data.diagnos);
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

  describe('vanligt', function() {
    beforeAll(function() {
      ValjIntygPage.get();
      specHelper.createUtkastForPatient('191212121212', 'lisjp');
    });

    describe('Fyll i intyget', function() {

      it('Spara undan intygsId från URL', function() {
        UtkastPage.disableAutosave();

        specHelper.getUtkastIdFromUrl().then(function(id) {
          utkastId = id;
        });
        data = wcTestTools.testdata.fk.LISJP.get(utkastId);
      });

      it('angeBaseratPa', function() {
        UtkastPage.angeBaseratPa(data.baseratPa);
      });
      it('angeSysselsättning', function() {
        UtkastPage.angeSysselsattning(data.sysselsattning);
      });
      it('angeDiagnos', function() {
        UtkastPage.angeDiagnos(data.diagnos);
      });
      it('angeFunktionsnedsattning', function() {
        UtkastPage.angeFunktionsnedsattning(data.funktionsnedsattning);
      });
      it('angeAktivitetsbegransning', function() {
        UtkastPage.angeAktivitetsbegransning(data.aktivitetsbegransning);
      });
      it('angeArbetsformaga', function() {
        UtkastPage.angeArbetsformaga(data.arbetsformaga);
      });
      it('angeArbetstidsforlaggning', function() {
        UtkastPage.angeArbetstidsforlaggning(data.arbetstidsforlaggning);
      });
      it('angeResorTillArbete', function() {
        UtkastPage.angeArbetsformaga(data.resorTillArbete);
      });
      it('angePrognosForArbetsformaga', function() {
        UtkastPage.angePrognosForArbetsformaga(data.prognosForArbetsformaga);
      });
      it('angeAtgarder', function() {
        UtkastPage.enableAutosave();
        UtkastPage.angeAtgarder(data.atgarder);
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
});
