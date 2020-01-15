/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

/* global logger, wcTestTools */

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)

/*
 *	Stödlib och ramverk
 *
 */

const {
  Given, // jshint ignore:line
  When, // jshint ignore:line
  Then // jshint ignore:line
} = require('cucumber');

var soap = require('soap');
var soapMessageBodies = require('./soap');
var helpers = require('./helpers');
var testvalues = wcTestTools.testdata.values;
var testdataHelpers = wcTestTools.helpers.testdata;
const path = '/services/create-draft-certificate/v3.0?wsdl';

/*
 *	Stödfunktioner
 *
 */

function sendCreateDraft(url, body, intyg) {
  return new Promise(function(resolve, reject) {
    soap.createClient(url, function(err, client) {
      logger.info(url);
      if (err) {
        logger.error('sendCreateDraft misslyckades' + err);
        throw (err);
      } else {
        client.CreateDraftCertificate(body, function(err, result, resBody) {
          logger.silly(resBody);
          if (err) {
            throw (err);
          } else {
            var resultcode = result.result.resultCode;
            logger.info('ResultCode: ' + resultcode);
            logger.silly(result);
            if (resultcode !== 'OK') {
              logger.info(result);
              logger.warn('ResultCode: ' + resultcode + '\n' + resBody);
            } else if (result['utlatande-id']) { //CD V1
              intyg.id = result['utlatande-id'].attributes.extension;
              logger.info('intyg.id: ' + intyg.id);
            } else if (result['intygs-id']) { //CD V2
              intyg.id = result['intygs-id'].extension;
              logger.info('intyg.id: ' + intyg.id);
            } else {
              throw ('Kunde inte hitta intygsid i svar');
            }
            resolve(expect(resBody).to.contain('OK'));
          }
        });
      }
    });
  });
}

function createDraft(intyg, patient, user) {
  var body;
  body = soapMessageBodies.CreateDraftCertificateV3(
      user,
      intyg.typ,
      patient
  );

  logger.silly(body);
  var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;
  url = url.replace('https', 'http');

  return sendCreateDraft(url, body, intyg);
}

/*
 *	Test steg
 *
 */

Then(/^ska vårdsystemet inte ha möjlighet att skapa "([^"]*)" utkast$/, function(intygstyp) {
  this.intyg.typ = intygstyp;

  let body = soapMessageBodies.CreateDraftCertificateV3(
      this.user,
      intygstyp,
      this.patient
  );
  let url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;
  url = url.replace('https', 'http');
  logger.silly(body);

  return new Promise(function(resolve, reject) {
    soap.createClient(url, function(err, client) {
      logger.info(url);
      if (err) {
        logger.error('sendCreateDraft misslyckades' + err);
        throw (err);
      } else {
        client.CreateDraftCertificate(body, function(err, result, resBody) {
          logger.silly(resBody);
          if (err) {
            throw (err);
          } else {
            resolve(expect(resBody).to.contain('Cannot issue intyg type'));
          }
        });
      }
    });
  });

});

Given(/^(?:att )vårdsystemet skapat ett intygsutkast( för samma patient)? för "([^"]*)"( med samordningsnummer)?$/,
    function(sammaPatient, intygstyp, samordningsnummer) {

      if (!sammaPatient) {
        this.patient = testdataHelpers.shuffle(testvalues.patienter)[0];
        if (samordningsnummer) {
          this.patient = testdataHelpers.shuffle(testvalues.patienterMedSamordningsnummer)[0];
        }
      }
      this.intyg.typ = intygstyp;
      return createDraft(this.intyg, this.patient, this.user);
    });

//Vid givet samEllerPersonNummer så shufflas det mellan person med vanligt personnummer och person med samordningsnummer
Given(
    /^(?:att )vårdsystemet skapat ett intygsutkast( för samma patient)? för slumpat (SMI\-)?(TS\-)?(AF\-)?intyg( med samordningsnummer eller personnummer)?$/,
    function(sammaPatient, smi, ts, af, samEllerPersonNummer) {

      if (!sammaPatient) {
        this.patient = testdataHelpers.shuffle(testvalues.patienter)[0];
        if (samEllerPersonNummer) {
          var shuffladPID = testdataHelpers.shuffle([testvalues.patienter, testvalues.patienterMedSamordningsnummer])[0];
          this.patient = testdataHelpers.shuffle(shuffladPID)[0];
        }
      }

      logger.debug('SMI: ' + (smi));
      logger.debug('TS: ' + (ts));
      logger.debug('AF: ' + (af));

      var intygtyper = [];

      if (smi) {
        intygtyper.push('Läkarintyg för sjukpenning',
            'Läkarutlåtande för sjukersättning',
            'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
        );
      } else if (ts) {
        intygtyper.push('Transportstyrelsens läkarintyg högre körkortsbehörighet',
            'Transportstyrelsens läkarintyg diabetes');
      } else if (af) {
        intygtyper.push('Arbetsförmedlingens medicinska utlåtande');
      } else {
        intygtyper.push(
            'Läkarintyg för sjukpenning',
            'Läkarutlåtande för sjukersättning',
            'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
            'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
            'Läkarintyg FK 7263',
            'Transportstyrelsens läkarintyg högre körkortsbehörighet',
            'Transportstyrelsens läkarintyg diabetes'
            //TODO aktivera DB-DOI
            //'Dödsbevis',
            //'Dödsorsaksintyg'
        );
      }

      this.intyg.typ = testdataHelpers.shuffle(intygtyper)[0];
      logger.info('Intyg typ: ' + this.intyg.typ + '\n');
      return createDraft(this.intyg, this.patient, this.user);
    });
