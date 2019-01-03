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

 /* globals logger*/

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


 /*
  *	Stödfunktioner
  *
  */


 /*
  *	Test steg
  *
  */

 When(/^jag skickar intyget direkt till Försäkringskassan$/, function(callback) {
     //logger.silly(personId);
     var url;
     var body;
     //logger.silly(intyg);
     var isSMIIntyg;
     if (this.intyg && this.intyg.typ) {
         isSMIIntyg = helpers.isSMIIntyg(this.intyg.typ);
     }

     if (isSMIIntyg) {
         logger.silly('is isSMIIntyg');
     } else {
         url = helpers.stripTrailingSlash(process.env.INTYGTJANST_URL) + '/send-certificate/v1.0?wsdl';
         url = url.replace('https', 'http');

         //function(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId)
         body = soapMessageBodies.SendMedicalCertificate(
             this.patient.id,
             this.user.hsaId,
             this.user.forNamn + ' ' + this.user.efterNamn,
             this.user.enhetId,
             this.user.enhetId,
             this.intyg.id);
         logger.silly(body);
         soap.createClient(url, function(err, client) {
             if (err) {
                 callback(err);
             }

             client.SendMedicalCertificate(body, function(err, result, body) {
                 if (err) {
                     throw err;
                 }
                 logger.silly(result);

                 var resultcode = result.result.resultCode;
                 logger.info('ResultCode: ' + resultcode);
                 if (resultcode !== 'OK') {
                     logger.info(result);
                     callback('ResultCode: ' + resultcode + '\n' + body);
                 } else {
                     callback();
                 }
             });
         });
     }
 });
