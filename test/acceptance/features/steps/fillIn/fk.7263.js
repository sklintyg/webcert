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

/* globals logg, pages, JSON, browser, Promise */

'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
module.exports = {
  fillIn:function(intyg,cb) {
		browser.ignoreSynchronization = true;
      console.log('eh');
      var promisesArr = [];
      //Ange smittskydd
      promisesArr.push(fkUtkastPage.angeSmittskydd(intyg.smittskydd).then(function () {
        logg('OK - angeSmittskydd :' + intyg.smittskydd);
      }, function (reason) {
        cb('FEL, angeSmittskydd,' + reason);
      }));

      //Ange baseras på
      promisesArr.push(fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa).then(function () {
        logg('OK - angeIntygetBaserasPa :' + JSON.stringify(intyg.baserasPa));
      }, function (reason) {
        cb('FEL, angeIntygetBaserasPa,' + reason);
      }));

      //Ange funktionsnedsättning
      promisesArr.push(fkUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning).then(function () {
        logg('OK - angeFunktionsnedsattning :' + JSON.stringify(intyg.funktionsnedsattning));
      }, function (reason) {
        cb('FEL, angeFunktionsnedsattning,' + reason);
      }));

      //Ange diagnoser
      promisesArr.push(fkUtkastPage.angeDiagnoser(intyg.diagnos).then(function () {
        logg('OK - angeDiagnoser :' + JSON.stringify(intyg.diagnos));
      }, function (reason) {
        cb('FEL, angeDiagnoser,' + reason);
      }));

      //Ange aktuellt sjukdomsförlopp
      promisesArr.push(fkUtkastPage.angeAktuelltSjukdomsForlopp(intyg.aktuelltSjukdomsforlopp).then(function () {
        logg('OK - angeAktuelltSjukdomsForlopp :' + JSON.stringify(intyg.aktuelltSjukdomsforlopp));
      }, function (reason) {
        cb('FEL, angeAktuelltSjukdomsForlopp,' + reason);
      }));

      //Ange aktivitetsbegränsning
      promisesArr.push(fkUtkastPage.angeAktivitetsBegransning(intyg.aktivitetsBegransning).then(function () {
        logg('OK - angeAktivitetsBegransning :' + JSON.stringify(intyg.aktivitetsBegransning));
      }, function (reason) {
        cb('FEL, angeAktivitetsBegransning,' + reason);
      }));

      promisesArr.push(fkUtkastPage.angeArbete(intyg.arbete).then(function () {
        logg('OK - angeArbete :' + JSON.stringify(intyg.arbete));
      }, function (reason) {
        cb('FEL, angeArbete,' + reason);
      }));
      promisesArr.push(fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function () {
        logg('OK - angeArbetsformaga :' + JSON.stringify(intyg.arbetsformaga));
      }, function (reason) {
        cb('FEL, angeArbetsformaga,' + reason);
      }));
      promisesArr.push(fkUtkastPage.angeArbetsformagaFMB(intyg.arbetsformagaFMB).then(function () {
        logg('OK - angeArbetsformagaFMB :' + JSON.stringify(intyg.arbetsformagaFMB));
      }, function (reason) {
        cb('FEL, angeArbetsformagaFMB,' + reason);
      }));

      promisesArr.push(fkUtkastPage.angePrognos(intyg.prognos).then(function () {
        logg('OK - angePrognos :' + JSON.stringify(intyg.prognos));
      }, function (reason) {
        cb('FEL, angePrognos,' + reason);
      }));
      promisesArr.push(fkUtkastPage.angeKontaktOnskasMedFK(intyg.kontaktOnskasMedFK).then(function () {
        logg('OK - angeKontaktOnskasMedFK :' + JSON.stringify(intyg.kontaktOnskasMedFK));
      }, function (reason) {
        cb('FEL, angeKontaktOnskasMedFK,' + reason);
      }));

      promisesArr.push(fkUtkastPage.angeRekommendationer(intyg.rekommendationer).then(function () {
        logg('OK - angeRekommendationer :' + JSON.stringify(intyg.rekommendationer));
      }, function (reason) {
        cb('FEL, angeRekommendationer,' + reason);
      }));

      return Promise.all(promisesArr)
      	.then(function (value) {
        	browser.ignoreSynchronization = false;
        	cb();
       	});
  }
};