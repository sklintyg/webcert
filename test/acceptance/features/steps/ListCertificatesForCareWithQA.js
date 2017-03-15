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
 /* globals logger, intyg, Promise */

 'use strict';
 var soap = require('soap');
 var soapMessageBodies = require('./soap');
 var helpers = require('./helpers');

 function sendListCertificatesForCareWithQA(body, callback) {
     var path = '/services/list-certificates-for-care-with-qa/v2.0?wsdl';
     var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;
     url = url.replace('https', 'http');
     return new Promise(function(resolve, reject) {
         soap.createClient(url, function(err, client) {
             logger.info(url);
             if (err) {
                 throw (err);
             } else {
                 client.ListCertificatesForCareWithQA(body, function(err, result, resBody) {
                     if (err) {
                         throw (err);
                     } else {
                         var resultcode = result.result.resultCode;
                         logger.info('ResultCode: ' + resultcode);
                         if (resultcode !== 'OK') {
                             logger.info(result);
                             throw ('ResultCode: ' + resultcode + '\n' + resBody);
                         } else {
                             resolve(result);
                         }
                     }
                 });
             }
         });


     });
 }

 var response;
 var responseIntyg;
 module.exports = function() {
     this.When(/^jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten$/, function() {
         var body = soapMessageBodies.ListCertificatesForCareWithQA.getBody(
             global.person.id,
             global.user.enhetId
         );
         console.log(body);
         return sendListCertificatesForCareWithQA(body).then(function(result) {
             response = result;
         });
     });


     this.Then(/^ska svaret( inte)? innehålla intyget jag var inne på$/, function(inte) {
         var idn = [];
         response.list.item.forEach(function(element) {
             var intygID = element.intyg['intygs-id'].extension;
             idn.push(intygID);
             if (intygID === intyg.id) {
                 responseIntyg = element;
             }
         });
         if (inte) {
             return expect(idn).to.not.include(intyg.id);
         }
         return expect(idn).to.include(intyg.id);
     });

     this.Then(/^ska svaret endast innehålla intyg för utvald patient$/, function() {
         var idPromises = [];
         response.list.item.forEach(function(element) {
             var personID = element.intyg.patient['person-id'].extension;
             idPromises.push(expect(personID).to.contain(global.person.id));
         });
         return Promise.all(idPromises);
     });

     this.Then(/^ska svaret endast innehålla intyg för vårdenheten$/, function() {
         var idPromises = [];
         response.list.item.forEach(function(element) {
             var enhetID = element.intyg.skapadAv.enhet['enhets-id'].extension;
             idPromises.push(expect(enhetID).to.contain(global.user.enhetId));
         });
         return Promise.all(idPromises);
     });


     this.Then(/^ska svaret visa intyghändelse "([^"]*)"$/, function(handelseKod) {
         var handelser = responseIntyg.handelser.handelse.map(function(obj) {
             return obj.handelsekod.code;
         });
         return expect(handelser).to.contain(handelseKod);
     });


 };
