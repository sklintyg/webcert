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
/*globals logger, wcTestTools*/

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
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('./helpers');

/*
 *	Stödfunktioner
 *
 */


/*
 *	Test steg
 *
 */


Given(/^ska (intyget|frågan) ha en indikator som indikerar sekretessmarkering$/, function(typ) {
    return expect(element(by.css('wc-sekretess-avliden-ikon')).isPresent()).to.eventually.become(true);
});

Given(/^Försäkringskassan skickar ett "([^"]*)" meddelande på intyget$/, function(amne, callback) {
    var body = soapMessageBodies.SendMessageToCare(this.user, this.patient, this.intyg, 'Begär ' + helpers.getSubjectFromCode(amne), testdataHelper.generateTestGuid(), amne);
    logger.silly(body);
    var path = '/send-message-to-care/v2.0?wsdl';
    var url = process.env.INTYGTJANST_URL + path;
    url = url.replace('https', 'http');

    soap.createClient(url, function(err, client) {
        logger.info(url);
        if (err) {
            callback(err);
        } else {
            client.SendMessageToCare(body, function(err, result, resBody) {
                logger.silly(resBody);
                if (err) {
                    callback(err);
                } else {
                    var resultcode = result.result.resultCode;
                    logger.info('ResultCode: ' + resultcode);
                    // logger.silly(result);
                    if (resultcode !== 'OK') {
                        logger.info(result);
                        callback('ResultCode: ' + resultcode + '\n' + resBody);
                    } else {
                        logger.info('ResultCode: ' + resultcode);
                        // logger.silly(JSON.stringify(result));
                        callback();
                    }

                }
            });
        }
    });
});
