 /*
  * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

 /* globals wcTestTools, logger*/

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


 var helpers = require('./helpers');
 var soap = require('soap');
 var soapMessageBodies = require('./soap');
 var testdataHelper = wcTestTools.helpers.testdata;
 var testvalues = wcTestTools.testdata.values;


 /*
  *	Stödfunktioner
  *
  */



 function intygTillIntygtjanst(intyg, patient, user, callback) {
     var url;
     var body;

     var isSMIIntyg;
     if (intyg && intyg.typ) {
         isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
     }
     console.log('isSMIIntyg: ' + isSMIIntyg);

     if (isSMIIntyg) {
         logger.silly('is isSMIIntyg');
         url = helpers.stripTrailingSlash(process.env.INTYGTJANST_URL) + '/register-certificate-se/v3.0?wsdl';
         url = url.replace('https', 'http');
         logger.silly(user);
         body = soapMessageBodies.RegisterCertificate(
             patient,
             user,
             intyg);
         logger.silly('URL - ' + url);
         logger.silly('BODY - ' + body);
         soap.createClient(url, function(err, client) {
             if (err) {
                 callback(err);
             }
             client.RegisterCertificate(body, function(err, result, body) {
                 logger.silly(err);
                 logger.silly(result);
                 var resultcodeSMI = result.result.resultCode;
                 logger.info('ResultCode: ' + resultcodeSMI);
                 if (resultcodeSMI !== 'OK') {
                     callback('ResultCode: ' + resultcodeSMI + '\n' + body);
                 } else {
                     callback();
                 }
                 if (err) {
                     callback(err);
                 } else {
                     callback();
                 }
             });
         });
     } else {

         url = helpers.stripTrailingSlash(process.env.INTYGTJANST_URL) + '/register-certificate/v3.0?wsdl';
         url = url.replace('https', 'http');
         //function(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId)
         body = soapMessageBodies.RegisterMedicalCertificate(
             patient.id,
             patient.forNamn,
             patient.efterNamn,
             user.hsaId,
             user.forNamn + ' ' + user.efterNamn,
             user.enhetId,
             user.enhetId,
             intyg.id);

         logger.silly(body);

         soap.createClient(url, function(err, client) {
             if (err) {
                 callback(err);
             }
             client.RegisterMedicalCertificate(body, function(err, result, body) {
                 logger.silly(err);
                 logger.silly(result);
                 var resultcode = result.result.resultCode;
                 logger.info('ResultCode: ' + resultcode);
                 if (resultcode !== 'OK') {
                     logger.info(result);
                     callback('ResultCode: ' + resultcode + '\n' + body);
                 } else {
                     callback();
                 }
                 if (err) {
                     callback(err);
                 } else {
                     callback();
                 }
             });
         });
     }

 }
 /*
  *	Test steg
  *
  */


 When(/^jag skickar ett "([^"]*)" intyg till Intygstjänsten$/, function(intygCode, callback) {
     this.intyg.id = testdataHelper.generateTestGuid();
     this.intyg.typ = intygCode;

     if (!this.patient || !this.patient.id) {
         this.patient = testdataHelper.shuffle(testvalues.patienter)[0];
     }

     //logger.silly(personId);
     //                'patientforNamn': 'Tolvan',
     //      'patientefterNamn': 'Tolvansson',

     intygTillIntygtjanst(this.intyg, this.patient, this.user, callback);

 });
 When(/^jag skickar ett intyg med ändrade personuppgifter till Intygstjänsten$/, function(callback) {
     this.intyg.id = testdataHelper.generateTestGuid();
     this.intyg.typ = 'Läkarintyg FK 7263';

     this.patient = testdataHelper.shuffle(testvalues.patienter)[0];
     this.patient.forNamn = 'forNamn';
     this.patient.efterNamn = 'efterNamn';
     logger.silly(this.intyg);
     intygTillIntygtjanst(this.intyg, this.patient, this.user, callback);
 });
 When(/^jag skickar ett SMI\-intyg till intygstjänsten på en avliden person$/, function(callback) {
     this.intyg.id = testdataHelper.generateTestGuid();
     this.intyg.typ = 'Läkarutlåtande för sjukersättning';

     this.patient = testdataHelper.shuffle(testvalues.patienterAvlidna)[0];
     this.patient.forNamn = 'forNamn';
     this.patient.efterNamn = 'efterNamn';
     logger.silly(this.intyg);
     intygTillIntygtjanst(this.intyg, this.patient, this.user, callback);


 });
