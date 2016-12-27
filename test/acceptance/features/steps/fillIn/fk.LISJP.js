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

 /* globals pages, browser, logger, Promise */

 'use strict';
 var lisjpUtkastPage = pages.intyg.lisjp.utkast;

 module.exports = {
     fillIn: function(intyg) {
         logger.info('intyg.typ:' + intyg.typ);
         browser.ignoreSynchronization = true;
         var promiseArr = [];

         //Baserat på
         promiseArr.push(lisjpUtkastPage.angeBaserasPa(intyg.baseratPa).then(function() {
             logger.info('OK - angeBaserasPa');
         }, function(reason) {
             throw ('FEL, angeBaserasPa,' + reason);
         }));

         // Sysselsättning
         promiseArr.push(lisjpUtkastPage.angeSysselsattning(intyg.sysselsattning).then(function() {
             logger.info('OK - angeSysselsattning');
         }, function(reason) {
             throw ('FEL, angeSysselsattning,' + reason);
         }));

         // Diagnos
         promiseArr.push(lisjpUtkastPage.angeDiagnos(intyg.diagnos).then(function() {
             logger.info('OK - angeDiagnos');
         }, function(reason) {
             throw ('FEL, angeDiagnos,' + reason);
         }));

         // Konsekvenser för patient
         promiseArr.push(lisjpUtkastPage.konsekvenser.funktionsnedsattning.sendKeys(intyg.funktionsnedsattning).then(function() {
             logger.info('OK - konsekvenser funktionsnedsattning');
         }, function(reason) {
             throw ('FEL, konsekvenser funktionsnedsattning, ' + reason);
         }));
         promiseArr.push(lisjpUtkastPage.konsekvenser.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning).then(function() {
             logger.info('OK - konsekvenser aktivitetsbegransning');
         }, function(reason) {
             throw ('FEL, konsekvenser aktivitetsbegransning,' + reason);
         }));

         // Bedöming Arbetsförmåga
         promiseArr.push(lisjpUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function() {
             logger.info('OK - angeArbetsformaga');
         }, function(reason) {
             throw ('FEL, angeArbetsformaga,' + reason);
         }));

         //Arbetstidsförläggning
         promiseArr.push(lisjpUtkastPage.angeArbetstidsforlaggning(intyg.arbetstidsforlaggning).then(function() {
             logger.info('OK - angeArbetstidsforlaggning');
         }, function(reason) {
             throw ('FEL, angeArbetstidsforlaggning,' + reason);
         }));


         //Trots FMB
         promiseArr.push(lisjpUtkastPage.sjukskrivning.forsakringsmedicinsktBeslutsstodBeskrivning.sendKeys(intyg.arbetsformagaFMB).then(function() {
             logger.info('OK - ange FMB');
         }, function(reason) {
             throw ('FEL, ange FMB,' + reason);
         }));

         //Resor till arbete
         promiseArr.push(lisjpUtkastPage.angeResorTillArbete(intyg.resorTillArbete).then(function() {
             logger.info('OK - angeResorTillArbete');
         }, function(reason) {
             throw ('FEL, angeResorTillArbete,' + reason);
         }));

         //Förmåga trots begränsning
         // promiseArr.push(lisjpUtkastPage.sjukskrivning.formagaTrotsBegransningBeskrivning.sendKeys(intyg.goraTrotsSjukdom).then(function() {
         //     logger.info('OK - formagaTrotsBegransningBeskrivning');
         // }, function(reason) {
         //     throw ('FEL, formagaTrotsBegransningBeskrivning,' + reason);
         // }));

         //Prognos
         promiseArr.push(lisjpUtkastPage.angePrognosForArbetsformaga(intyg.prognosForArbetsformaga).then(function() {
             logger.info('OK - prognosForArbetsformaga');
         }, function(reason) {
             throw ('FEL, prognosForArbetsformaga,' + reason);
         }));

         // Åtgärd
         promiseArr.push(lisjpUtkastPage.angeAtgarder(intyg.atgarder).then(function() {
             logger.info('OK - angeAtgarder');
         }, function(reason) {
             throw ('FEL, angeAtgarder,' + reason);
         }));
         browser.ignoreSynchronization = false;
         return Promise.all(promiseArr);
     }
 };
