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

/**
 * Created by BESA on 2015-11-25.
 */
/*globals logger */
'use strict';

var restUtil = require('./../util/rest.util.js');
var arendeFromJsonFactory = require('./../util/arendeFromJsonFactory.js');
var fragasvarFromJsonFactory = require('./../util/fragasvarFromJsonFactory.js');
var intygGenerator = require('./../util/intygGenerator.util.js');

function createArende(createJson) {
  restUtil.login();
  return restUtil.createArende(createJson);
}

function createFragasvar(createJson) {
  restUtil.login();
  return restUtil.createFragasvar(createJson);
}

module.exports = {

  // Intyg services
  deleteAllIntyg: function() {
    restUtil.login();
    return restUtil.deleteAllIntyg();
  },
  deleteAllIntygForPatient: function(personnummer) {
    restUtil.login();
    return restUtil.deleteAllIntygForPatient(personnummer);
  },
  deleteIntyg: function(id) {
    restUtil.login();
    return restUtil.deleteIntyg(id);
  },
  createWebcertIntyg: function(createJson) {
    restUtil.login();
    return restUtil.createWebcertIntyg(createJson);
  },
  createEmptyUtkast: function(intygType, intygTypeVersion, utkastId) {
    restUtil.login();

    var testData = {
      'contents': intygGenerator.getEmptyUtkastJson(intygType, intygTypeVersion, utkastId),
      'utkastStatus': 'DRAFT_INCOMPLETE',
      'revoked': false
    };

    return restUtil.createWebcertIntyg(testData);
  },
  createUtkast: function(intygType, template) {
    restUtil.login();

    var utkastTemplate = template;
    if (typeof template === 'undefined') {
      utkastTemplate = {
        'intygType': intygType,
        'patientPersonnummer': '19121212-1212',
        'patientFornamn': 'Tolvan',
        'patientEfternamn': 'Tolvansson',
        'patientPostadress': 'Svensson, Storgatan 1, PL 1234',
        'patientPostnummer': '12345',
        'patientPostort': 'Småmåla'
      };
    }
    return restUtil.createUtkast(intygType, utkastTemplate);
  },
  saveUtkast: function(intygType, intygId, version, utkastJson) {
    return restUtil.saveUtkast(intygType, intygId, version, utkastJson);
  },
  markeraSkickatTillFK: function(id) {
    return restUtil.markeraSkickatTillFK(id);
  },

  deleteAllUtkast: function() {
    restUtil.login();
    return restUtil.deleteAllUtkast();
  },
  deleteAllUtkastForPatient: function(personnummer) {
    restUtil.login();
    return restUtil.deleteAllUtkastForPatient(personnummer);
  },
  deleteUtkast: function(id) {
    restUtil.login();
    return restUtil.deleteUtkast(id);
  },
  deleteHandelserForUtkast: function(id) {
    restUtil.login();
    return restUtil.deleteHandelserForUtkast(id);
  },
  deleteHandelserForPatient: function(id) {
    restUtil.login();
    return restUtil.deleteHandelserForPatient(id);
  },

  // Ärenden
  createArende: createArende,
  createArendeFromTemplate: function(intygType, intygId, meddelandeId, meddelande, amne, status, komplettering,
      svarPa) {
    var arende = arendeFromJsonFactory.get({
      intygType: intygType,
      intygId: intygId,
      meddelandeId: meddelandeId,
      meddelande: 'Hur är det med arbetstiden?',
      amne: amne,
      status: status,
      kompletteringar: komplettering
    });

    if (svarPa) {
      arende.svarPa = svarPa;
    }

    logger.debug('arende to be created: ' + JSON.stringify(arende));
    createArende(arende).then(function(response) {
      logger.debug('response code:' + response.statusCode);
    });
  },
  deleteAllArenden: function() {
    logger.debug('deleting all arenden');
    restUtil.login();
    return restUtil.deleteAllArenden();
  },
  deleteArende: function(id) {
    restUtil.login();
    return restUtil.deleteArende(id);
  },

  // Fråga/svar
  createFragasvar: createFragasvar,

  /*
   fragaSvar.amne = fragaSvarOptions.amne;
   fragaSvar.internReferens = fragaSvarOptions.internReferens;
   fragaSvar.intygsReferens.intygsTyp = fragaSvarOptions.intygType;
   fragaSvar.intygsReferens.intygsId = fragaSvarOptions.intygId;
   fragaSvar.intygsReferens.patientId = fragaSvarOptions.patientId;
   fragaSvar.frageText = fragaSvarOptions.frageText;
   fragaSvar.svarsText = fragaSvarOptions.svarsText;
   fragaSvar.meddelandeRubrik = fragaSvarOptions.meddelandeRubrik;
   fragaSvar.status = fragaSvarOptions.status;
   fragaSvar.kompletteringar = fragaSvarOptions.kompletteringar;
   fragaSvar.vardperson = fragaSvarOptions.vardperson;
   fragaSvar.externaKontakter = [];
   fragaSvar.vidarebefordrad = fragaSvarOptions.vidarebefordrad;
   */
  createFragasvarFromTemplate: function(internReferens, intygsId, patientId, amne, status, komplettering,
      vidarebefordrad, callback) {
    var fraga = fragasvarFromJsonFactory.get({
      internReferens: internReferens,

      intygsId: intygsId,
      intygsTyp: 'fk7263',
      patientId: patientId,

      frageText: 'Hur är det med arbetstiden?',
      amne: amne,
      status: status,
      kompletteringar: [komplettering],
      vidarebefordrad: vidarebefordrad
    });

    logger.debug('fragasvar to be created: ' + JSON.stringify(fraga));
    createFragasvar(fraga).then(function(response) {
      logger.debug('response code:' + response.statusCode);
      logger.debug('response output: ' + response.body.internReferens);
      callback(response.body.internReferens);
    });
  },

  setSekretessmarkering: function(personId, isSekretessmarkerad) {
    if (personId.indexOf('-') > -1) {
      personId = personId.replace('-', '');
    }
    restUtil.login();
    return restUtil.setSekretessmarkering(personId, isSekretessmarkerad);
  },
  createBanners: function(message, prio) {
    var toDate = new Date();
    toDate.setFullYear(toDate.getFullYear() + 1);

    var banner = {
      message: message,
      priority: prio,
      application: 'WEBCERT',
      createdAt: new Date().toISOString().substring(0, 19),
      displayFrom: new Date().toISOString().substring(0, 19),
      displayTo: toDate.toISOString().substring(0, 19)
    };

    return restUtil.createBanners(banner);
  },
  clearBanners: function() {
    return restUtil.clearBanners();
  }

};
