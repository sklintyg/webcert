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

/*globals pages, logger, browser, Promise */

'use strict';
var doiUtkastPage = pages.intyg.soc.doi.utkast;
//var helpers = require('../helpers');

module.exports = {
  fillIn: function(intyg) {

    //Returnera Promise kedja
    return new Promise(function(resolve) {
      logger.info('Fyller i ' + intyg.typ + ' formuläret synkront');
      browser.ignoreSynchronization = true;
      resolve('Fyller i ' + intyg.typ + '  formuläret synkront');
    })
    .then(function() {
      logger.info('Fyller i patient address det sista vi gör (common)');
    })
    .then(function() {
      //Kompletterande patientuppgifter
      return doiUtkastPage.angeIdentitetStyrktGenom(intyg.identitetStyrktGenom)
      .then(function() {
        logger.info('OK - angeIdentitetStyrktGenom');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeIdentitetStyrktGenom,' + reason);
      });
    })
    .then(function() {
      //Dödsdatum
      return doiUtkastPage.angeDodsdatum(intyg.dodsdatum)
      .then(function() {
        logger.info('OK - angeDodsdatum');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeDodsdatum,' + reason);
      });

    }).then(function() {
      //Dödsplats
      return doiUtkastPage.angeDodsPlats(intyg.dodsPlats)
      .then(function() {
        logger.info('OK - angeDodsPlats');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeDodsPlats,' + reason);
      });
    }).then(function() {
      //Barn som avlidit senast 28 dygn efter födelsen
      return doiUtkastPage.angeBarn(intyg.barn)
      .then(function() {
        logger.info('OK - angeBarn');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeBarn,' + reason);
      });
    }).then(function() {
      //Läkarens utlåtande om dödsorsaken 
      return doiUtkastPage.angeUtlatandeOmDodsorsak(intyg.dodsorsak)
      .then(function() {
        logger.info('OK - angeUtlatandeOmDodsorsak');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeUtlatandeOmDodsorsak,' + reason);
      });
    }).then(function() {
      //Opererad inom fyra veckor före döden
      return doiUtkastPage.angeOperation(intyg.operation)
      .then(function() {
        logger.info('OK - angeOperation');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeOperation,' + reason);
      });
    }).then(function() {
      //SkadaForgiftning
      return doiUtkastPage.angeSkadaForgiftning(intyg.skadaForgiftning)
      .then(function() {
        logger.info('OK - angeSkadaForgiftning');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeSkadaForgiftning,' + reason);
      });
    }).then(function() {
      //Dödsorsaksuppgifter
      return doiUtkastPage.angeDodsorsaksuppgifterna(intyg.dodsorsaksuppgifter)
      .then(function() {
        logger.info('OK - angeDodsorsaksuppgifterna');
      }, function(reason) {
        console.trace(reason);
        throw ('FEL, angeDodsorsaksuppgifterna,' + reason);
      });
    });

  }
};
