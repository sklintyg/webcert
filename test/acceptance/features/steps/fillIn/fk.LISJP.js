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

 /* globals pages, logger, browser, Promise */

 'use strict';
 var lisjpUtkastPage = pages.intyg.lisjp.utkast;

 module.exports = {
     fillIn: function(intyg) {

         //Returnera Promise kedja
         return new Promise(function(resolve) {
                 logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
                 browser.ignoreSynchronization = true;
                 resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
             })
             .then(function() {
                 //Baserat på
                 return lisjpUtkastPage.angeBaseratPa(intyg.baseratPa)
                     .then(function() {
                         logger.info('OK - angeBaseratPa');
                     }, function(reason) {
                         throw ('FEL, angeBaseratPa,' + reason);
                     });
             })
             .then(function() {
                 // Sysselsättning
                 return lisjpUtkastPage.angeSysselsattning(intyg.sysselsattning).then(function() {
                     logger.info('OK - angeSysselsattning');
                 }, function(reason) {
                     throw ('FEL, angeSysselsattning,' + reason);
                 });
             })
             .then(function() {
                 // Diagnos
                 return lisjpUtkastPage.angeDiagnos(intyg.diagnos).then(function() {
                     logger.info('OK - angeDiagnos');
                 }, function(reason) {
                     throw ('FEL, angeDiagnos,' + reason);
                 });
             })
             .then(function() {
                 // Konsekvenser - funktionsnedsattning för patient
                 return lisjpUtkastPage.konsekvenser.funktionsnedsattning.sendKeys(intyg.funktionsnedsattning).then(function() {
                     logger.info('OK - konsekvenser funktionsnedsattning');
                 }, function(reason) {
                     throw ('FEL, konsekvenser funktionsnedsattning, ' + reason);
                 });
             })
             .then(function() {
                 // Konsekvenser - aktivitetsbegransning för patient
                 return lisjpUtkastPage.konsekvenser.aktivitetsbegransning.sendKeys(intyg.aktivitetsbegransning).then(function() {
                     logger.info('OK - konsekvenser aktivitetsbegransning');
                 }, function(reason) {
                     throw ('FEL, konsekvenser aktivitetsbegransning,' + reason);
                 });
             })
             .then(function() {
                 logger.info('TODO - Medicinsk behandling');
                 return;
             })
             .then(function() {
                 // Bedöming Arbetsförmåga
                 return lisjpUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function() {
                     logger.info('OK - angeArbetsformaga');
                 }, function(reason) {
                     throw ('FEL, angeArbetsformaga,' + reason);
                 });
             })
             .then(function() {
                 //Arbetstidsförläggning
                 return lisjpUtkastPage.angeArbetstidsforlaggning(intyg.arbetstidsforlaggning).then(function() {
                     logger.info('OK - angeArbetstidsforlaggning');
                 }, function(reason) {
                     throw ('FEL, angeArbetstidsforlaggning,' + reason);
                 });
             })
             .then(function() {
                 //Trots FMB
                 return lisjpUtkastPage.sjukskrivning.forsakringsmedicinsktBeslutsstodBeskrivning.sendKeys(intyg.arbetsformagaFMB).then(function() {
                     logger.info('OK - ange FMB');
                 }, function(reason) {
                     throw ('FEL, ange FMB,' + reason);
                 });
             })
             .then(function() {
                 //Resor till arbete
                 return lisjpUtkastPage.angeResorTillArbete(intyg.resorTillArbete).then(function() {
                     logger.info('OK - angeResorTillArbete');
                 }, function(reason) {
                     throw ('FEL, angeResorTillArbete,' + reason);
                 });
             })
             .then(function() {
                 //Prognos
                 return lisjpUtkastPage.angePrognosForArbetsformaga(intyg.prognosForArbetsformaga).then(function() {
                     logger.info('OK - prognosForArbetsformaga');
                 }, function(reason) {
                     throw ('FEL, prognosForArbetsformaga,' + reason);
                 });
             })
             .then(function() {
                 // Åtgärd
                 return lisjpUtkastPage.angeAtgarder(intyg.atgarder).then(function() {
                     logger.info('OK - angeAtgarder');
                 }, function(reason) {
                     throw ('FEL, angeAtgarder,' + reason);
                 });
             })
             .then(function() {
                 logger.info('TODO - Övriga Upplysningar');
                 return;
             })
             .then(function() {
                 logger.info('TODO - Kontakt med FK');
                 return;
             })
             .then(function() {
                 logger.info('TODO - Tillägsfrågor');
                 return;
             })
             .then(function() {
                 browser.ignoreSynchronization = false;
             });

     }
 };
