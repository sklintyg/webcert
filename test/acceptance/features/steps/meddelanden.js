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
/*globals logger, wcTestTools, intyg*/

'use strict';
var soap = require('soap');
var soapMessageBodies = require('./soap');
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('./helpers');

module.exports = function() {
    // });

    this.Given(/^ska (intyget|frågan) ha en indikator som indikerar sekretessmarkering$/, function(typ) {

        var elm;

        if (typ === 'frågan') {
            elm = 'wc-sekretessmarkering-icon-' + global.meddelanden[0].id;
            console.log(elm);
        } else if (typ === 'intyget') {
            //Annars kollar vi efter 'icon+intyg' elemenetet
            elm = 'wc-sekretessmarkering-icon-' + intyg.id;
        }

        return expect(element(by.id(elm)).isPresent()).to.eventually.become(true);

    });

    this.Given(/^Försäkringskassan skickar ett "([^"]*)" meddelande på intyget$/, function(amne, callback) {
        global.intyg.guidcheck = testdataHelper.generateTestGuid();

        var body = soapMessageBodies.SendMessageToCare(global.user, global.person, global.intyg, 'Begär ' + helpers.getSubjectFromCode(amne) + ' ' + global.intyg.guidcheck, amne);
        console.log(body);
        var path = '/send-message-to-care/v2.0?wsdl';
        var url = process.env.INTYGTJANST_URL + path;
        url = url.replace('https', 'http');

        soap.createClient(url, function(err, client) {
            logger.info(url);
            if (err) {
                callback(err);
            } else {
                client.SendMessageToCare(body, function(err, result, resBody) {
                    console.log(resBody);
                    if (err) {
                        callback(err);
                    } else {
                        var resultcode = result.result.resultCode;
                        logger.info('ResultCode: ' + resultcode);
                        // console.log(result);
                        if (resultcode !== 'OK') {
                            logger.info(result);
                            callback('ResultCode: ' + resultcode + '\n' + resBody);
                        } else {
                            logger.info('ResultCode: ' + resultcode);
                            // console.log(JSON.stringify(result));
                            callback();
                        }

                    }
                });
            }
        });
    });



};
