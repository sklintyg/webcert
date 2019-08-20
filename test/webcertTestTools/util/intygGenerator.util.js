/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
 * Created by BESA on 2015-11-23.
 */

/* globals JSON, logger*/
'use strict';

/*
 Options
 {
 personnr : '',
 patientNamn : '',
 issuerId : '',
 issuer : '',
 issued : '',
 validFrom : '',
 validTo : '',
 enhetId : '',
 enhet : '',
 vardgivarId : '',
 intygType : '',
 intygId : '',
 idTemplate : '',
 mall : '',
 from : '',
 to : '',
 sent : '',
 revoked : '',
 deletedByCareGiver : '',
 responseStatus : '',
 template : ''
 }
 */
function _getIntygJson(intygOptions) {
  var intyg = require('./../testdata/intyg.' + intygOptions.intygType + '.json');
  if (intygOptions.intygId) {
    intyg.id = intygOptions.intygId;
  }
  return intyg;
}

function _getEmptyUtkastJson(intygType, intygVersion, intygId) {
  var intyg = require('./../testdata/utkast.empty.json');

  intyg.id = intygId;
  intyg.typ = intygType;
  intyg.textVersion = intygVersion;

  return intyg;
}

function _buildIntyg(intygOptions) {

  //    logger.info("======================================================INTYG");
  //    logger.info(fkIntyg);
  var intygTemplate = require('./../testdata/intyg.' + intygOptions.intygType + '.json');
  var jsonDocument = buildDocumentFromIntygTemplate(intygTemplate, intygOptions);
  //    logger.info("======================================================JSONDOCUMENT");
  //    logger.info(jsonDocument);

  var stateList = [{
    state: 'RECEIVED',
    target: 'HSVARD',
    timestamp: intygOptions.issued + 'T12:00:00.000'
  }];
  if (intygOptions.sent) {
    stateList.push({
      state: 'SENT',
      target: 'FKASSA',
      timestamp: intygOptions.issued + 'T12:00:10.000'
    });
  }
  if (intygOptions.revoked) {
    stateList.push({
      state: 'CANCELLED',
      target: 'HSVARD',
      timestamp: intygOptions.issued + 'T13:00:00.000'
    });
  }

  var additionalInfo = '';
  if (intygOptions.intygType === 'fk7263') {
    additionalInfo = intygOptions.validFrom + ' - ' + intygOptions.validTo;
    /*  }  else if (intygType.equalsIgnoreCase('ts-bas') || intygType.equalsIgnoreCase('ts-diabetes')) {
            def korkortstyper = certificate.intygAvser.korkortstyp*.type
            additionalInfo = '${korkortstyper.join(', ')}'*/
  }

  /*    reset() {
          mall = "M"
          utfärdarId = "EttUtfärdarId"
          utfärdare = "EnUtfärdare"
          enhetsId = "1.2.3"
          giltigtFrån = null
          giltigtTill = null
          template = null
          skickat = false
          rättat = false
          deletedByCareGiver = false
      }
    */
  //    logger.info("======================================================INTYGOPTIONS-2");
  //    logger.info(intygOptions);

  var resultIntyg = {
    id: intygOptions.intygId, // id, issued??
    type: intygOptions.intygType,
    civicRegistrationNumber: intygOptions.personnr,
    signedDate: intygOptions.issued,
    signingDoctorName: intygOptions.issuer,
    validFromDate: intygOptions.validFrom,
    validToDate: intygOptions.validTo,
    careUnitId: (intygOptions.enhetId) ? intygOptions.enhetId : '1.2.3',
    careUnitName: intygOptions.enhet ? intygOptions.enhet : 'Enheten',
    careGiverId: intygOptions.vardgivarId ? intygOptions.vardgivarId : '4.5.6',
    deletedByCareGiver: typeof intygOptions.deletedByCareGiver === 'undefined' ? false : true,
    additionalInfo: additionalInfo,
    certificateStates: stateList,
    document: jsonDocument
  };

  //    logger.info("======================================================RESULTINTYG");
  //    logger.info(resultIntyg);
  return resultIntyg;
}

function buildDocumentFromIntygTemplate(intyg, intygOptions) {

  // setting the certificate ID
  intyg.id = intygOptions.intygId;

  // setting personnr in certificate XML
  intyg.grundData.patient.personId = intygOptions.personnr;

  // Ange patientens namn
  if (intygOptions.patientNamn) {
    intyg.grundData.patient.fullstandigtNamn = intygOptions.patientNamn;
  }
  if (intygOptions.issuerId) {
    intyg.grundData.skapadAv.personId = intygOptions.issuerId;
  }
  if (intygOptions.issuer) {
    intyg.grundData.skapadAv.fullstandigtNamn = intygOptions.issuer;
  }
  if (intygOptions.enhetId) {
    intyg.grundData.skapadAv.vardenhet.enhetsid = intygOptions.enhetId;
  }
  if (intygOptions.enhet) {
    intyg.grundData.skapadAv.vardenhet.enhetsnamn = intygOptions.enhet;
  }
  if (intygOptions.vardgivarId) {
    intyg.grundData.skapadAv.vardenhet.vardgivare.vardgivarid = intygOptions.vardgivarId;
  }

  // setting the signing date, from date and to date
  intyg.grundData.signeringsdatum = intygOptions.issued;
  if (intygOptions.intygType === 'fk7263') {
    overrideFkDefaults(intyg, intygOptions);
  } else if (intygOptions.intygType === 'ts-bas') {
    overridetsBasDefaults(intyg, intygOptions);
  }

  return JSON.stringify(intyg);
}

function addBaserasPaDates(intyg, issuedDate) {
  if (intyg.undersokningAvPatienten) {
    intyg.undersokningAvPatienten = issuedDate;
  }
  if (intyg.telefonkontaktMedPatienten) {
    intyg.telefonkontaktMedPatienten = issuedDate;
  }
  if (intyg.journaluppgifter) {
    intyg.journaluppgifter = issuedDate;
  }
  if (intyg.annanReferens) {
    intyg.annanReferens = issuedDate;
  }

  return intyg;

}

function overrideFkDefaults(intyg, intygOptions) {

  intyg = addBaserasPaDates(intyg, intygOptions.issued);

  //Nedsatt med 100
  if (intyg.nedsattMed100 && !intyg.nedsattMed100.from) {
    intyg.nedsattMed100.from = intygOptions.validFrom;
  }
  if (intyg.nedsattMed100 && !intyg.nedsattMed100.tom) {
    intyg.nedsattMed100.tom = intygOptions.validTo;
  }

  //Nedsatt med 75
  if (intyg.nedsattMed75 && !intyg.nedsattMed75.from) {
    intyg.nedsattMed75.from = intygOptions.validFrom;
  }
  if (intyg.nedsattMed75 && !intyg.nedsattMed75.tom) {
    intyg.nedsattMed75.tom = intygOptions.validTo;
  }

  //Nedsatt med 50
  if (intyg.nedsattMed50 && !intyg.nedsattMed50.from) {
    intyg.nedsattMed50.from = intygOptions.validFrom;
  }
  if (intyg.nedsattMed50 && !intyg.nedsattMed50.tom) {
    intyg.nedsattMed50.tom = intygOptions.validTo;
  }

  //Nedatt med 25
  if (intyg.nedsattMed25 && !intyg.nedsattMed25.from) {
    intyg.nedsattMed25.from = intygOptions.validFrom;
  }
  if (intyg.nedsattMed25 && !intyg.nedsattMed25.tom) {
    intyg.nedsattMed25.tom = intygOptions.validTo;
  }

  intyg.giltighet.from = intygOptions.validFrom;
  intyg.giltighet.tom = intygOptions.validTo;
}

function overridetsBasDefaults(intyg, intygOptions) {

  intyg = addBaserasPaDates(intyg, intygOptions.issued);

  logger.info('Inside intygOptions.intygType === ' + intygOptions.intygType);
  logger.info('höger öga utan korrekt: ' + intygOptions.intygAvser.syn.hogerOga.utanKorrektion);
  logger.info('PatientId: ' + intygOptions.grundData.patient.personId);

}

module.exports = {
  getIntygJson: _getIntygJson,
  getEmptyUtkastJson: _getEmptyUtkastJson,
  buildIntyg: _buildIntyg
};
