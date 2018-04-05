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

/* global intyg, logger,wcTestTools */

'use strict';
var soap = require('soap');
var soapMessageBodies = require('./soap');
var helpers = require('./helpers');
var testvalues = wcTestTools.testdata.values;
var testdataHelpers = wcTestTools.helpers.testdata;

function sendCreateDraft(url, body, callback) {
    soap.createClient(url, function(err, client) {
        logger.info(url);
        if (err) {
            logger.error('sendCreateDraft misslyckades' + err);
            callback(err);
        } else {
            client.CreateDraftCertificate(body, function(err, result, resBody) {
                console.log(resBody);
                if (err) {
                    callback(err);
                } else {
                    var resultcode = result.result.resultCode;
                    logger.info('ResultCode: ' + resultcode);
                    console.log(result);
                    if (resultcode !== 'OK') {
                        logger.info(result);
                        callback('ResultCode: ' + resultcode + '\n' + resBody);
                    } else if (result['utlatande-id']) { //CD V1
                        intyg.id = result['utlatande-id'].attributes.extension;
                        logger.info('intyg.id: ' + intyg.id);
                        callback();
                    } else if (result['intygs-id']) { //CD V2
                        intyg.id = result['intygs-id'].extension;
                        logger.info('intyg.id: ' + intyg.id);
                        callback();
                    } else {
                        callback('Kunde inte hitta intygsid i svar');
                    }
                }
            });
        }
    });
}

function createBody(intygstyp, callback) {
    global.intyg.typ = intygstyp;

    var body, path;
    var isSMIIntyg = helpers.isSMIIntyg(intygstyp);
    var isTSIntyg = helpers.isTSIntyg(intygstyp);

    if (isSMIIntyg || isTSIntyg) {
        path = '/services/create-draft-certificate/v3.0?wsdl';
        body = soapMessageBodies.CreateDraftCertificateV3(
            global.user,
            intygstyp
        );

    } else {
        path = '/services/create-draft-certificate/v1.0?wsdl';
        body = soapMessageBodies.CreateDraftCertificate(
            global.user.hsaId,
            global.user.forNamn + ' ' + global.user.efterNamn,
            global.user.enhetId,
            'Enhetsnamn',
            global.person.id
        );
    }
    console.log(body);
    var url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;
    url = url.replace('https', 'http');

    sendCreateDraft(url, body, callback);
}

module.exports = function() {
    this.Given(/^(?:att )vårdsystemet skapat ett intygsutkast( för samma patient)? för "([^"]*)"( med samordningsnummer)?$/, function(sammaPatient, intygstyp, samordningsnummer, callback) {

        if (!sammaPatient) {
            global.person = testdataHelpers.shuffle(testvalues.patienter)[0];
            if (samordningsnummer) {
                global.person = testdataHelpers.shuffle(testvalues.patienterMedSamordningsnummer)[0];
            }
        }
        createBody(intygstyp, callback);
    });

    //Vid givet samEllerPersonNummer så shufflas det mellan person med vanligt personnummer och person med samordningsnummer
    this.Given(/^(?:att )vårdsystemet skapat ett intygsutkast( för samma patient)? för slumpat (SMI\-)?(TS\-)?intyg( med samordningsnummer eller personnummer)?$/,
        function(sammaPatient, smi, ts, samEllerPersonNummer, callback) {

            if (!sammaPatient) {
                global.person = testdataHelpers.shuffle(testvalues.patienter)[0];
                if (samEllerPersonNummer) {
                    var shuffladPID = testdataHelpers.shuffle([testvalues.patienter, testvalues.patienterMedSamordningsnummer])[0];
                    global.person = testdataHelpers.shuffle(shuffladPID)[0];
                }
            }



            logger.debug('SMI: ' + (smi));
            logger.debug('TS: ' + (ts));

            var intygtyper = [];

            if (smi) {
                intygtyper.push('Läkarintyg för sjukpenning',
                    'Läkarutlåtande för sjukersättning',
                    'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
                    'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
                );
            } else if (ts) {
                intygtyper.push('Transportstyrelsens läkarintyg',
                    'Transportstyrelsens läkarintyg, diabetes');
            } else {
                intygtyper.push(
                    'Läkarintyg för sjukpenning',
                    'Läkarutlåtande för sjukersättning',
                    'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
                    'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
                    'Läkarintyg FK 7263',
                    'Transportstyrelsens läkarintyg',
                    'Transportstyrelsens läkarintyg, diabetes'
                    //TODO aktivera DB-DOI
                    //'Dödsbevis',
                    //'Dödsorsaksintyg'
                );
            }

            var randomIntygType = testdataHelpers.shuffle(intygtyper)[0];
            logger.info('Intyg typ: ' + randomIntygType + '\n');
            createBody(randomIntygType, callback);
        });
};
