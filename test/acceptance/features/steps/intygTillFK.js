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

 /* globals intyg, logger*/

 'use strict';

 var helpers = require('./helpers');
 var soap = require('soap');
 var soapMessageBodies = require('./soap');

 module.exports = function() {

     this.Given(/^jag skickar intyget direkt till Försäkringskassan$/, function(callback) {
         //console.log(personId);
         var url;
         var body;
         //console.log(intyg);
         var isSMIIntyg;
         if (intyg && intyg.typ) {
             isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
         }

         if (isSMIIntyg) {
             console.log('is isSMIIntyg');
         } else {
             url = helpers.stripTrailingSlash(process.env.INTYGTJANST_URL) + '/send-certificate/v1.0?wsdl';
             url = url.replace('https', 'http');

             //function(personId, doctorHsa, doctorName, unitHsa, unitName, intygsId)
             body = soapMessageBodies.SendMedicalCertificate(
                 global.person.id,
                 global.user.hsaId,
                 global.user.fornamn + ' ' + global.user.efternamn,
                 global.user.enhetId,
                 global.user.enhetId,
                 global.intyg.id);
             console.log(body);
             soap.createClient(url, function(err, client) {
                 if (err) {
                     callback(err);
                 }

                 client.SendMedicalCertificate(body, function(err, result, body) {
                     if (err) {
                         throw err;
                     }
                     console.log(result);

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
 };
