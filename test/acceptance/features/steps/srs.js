/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

/* globals pages, browser */
'use strict';
let helpers = require('./helpers');
let Soap = require('soap');
let soapMessageBodies = require('./soap');
let fk7263utkast = pages.intyg.fk['7263'].utkast;
let srsdata = require('./srsdata.js');

module.exports = function() {
    let user = {};

    this.Given(/^att jag är djupintegrerat inloggad som läkare på vårdenhet "(med SRS|utan SRS)"$/,
        srsStatus => {
            user = srsdata.inloggningar[srsStatus];
            return pages.welcome.get()
                .then(() => pages.welcome.loginByJSON(JSON.stringify(user), true));
        }

    );

    this.Given(/^att jag befinner mig på ett nyskapat Läkarintyg FK 7263 för en patient som "(inte har givit samtycke|har givit samtycke)" till SRS$/,
        samtycke =>
        createDraftUsingSOAP(user, srsdata.patienter[samtycke].id)
        .then(intygsId => browser.get(buildLinkToIntyg(intygsId, srsdata.patienter[samtycke], user.enhetId)))
        .then(() => browser.waitForAngular())
        .then(() => browser.sleep(2000))
        .then(() => expect(element(by.id('wcHeader')).isPresent()).to.eventually.equal(true))
    );

    this.When(/^jag fyller i diagnoskod som "(finns i SRS|inte finns i SRS)"$/,
        srsStatus => fk7263utkast.angeDiagnosKod(srsdata.diagnoskoder[srsStatus])
    );

    this.Then(/^ska knappen för SRS vara i läge "(stängd|öppen|gömd)"$/,
        srsButtonStatus => expect(fk7263utkast.getSRSButtonStatus()).to.eventually.equal(srsButtonStatus)
    );

};

function createDraftUsingSOAP(user, patientId) {
    let path = '/services/create-draft-certificate/v1.0/?wsdl';
    let body = soapMessageBodies.CreateDraftCertificate(
        user.hsaId,
        `${user.forNamn} ${user.efterNamn}`,
        user.enhetId,
        'Enhetsnamn',
        patientId
    );

    let url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;

    return new Promise((resolve, reject) =>
        Soap.createClient(url, (err, client) => {
            client.CreateDraftCertificate(body, (err, response, responseBody) => {
                if (isNotOk(response)) {
                    reject(`Felaktigt SOAP-svar: ${responseBody}`);
                } else if (err) {
                    reject(err);
                } else {
                    resolve(response['utlatande-id'].attributes.extension);
                }
            });
        })
    );
}

function isNotOk(response) {
    return !response || !response.result || response.result.resultCode !== 'OK' || !response['utlatande-id'] || !response['utlatande-id'].attributes;
}

function buildLinkToIntyg(intygsId, patient, enhetsId) {
    let uri = uriTemplate `visa/intyg/${intygsId}?fornamn=${patient.fornamn}&efternamn=${patient.efternamn}&postadress=${patient.adress.postadress}&postnummer=${patient.adress.postnummer}&postort=${patient.adress.postort}&enhet=${enhetsId}`;
    console.log('IntygsURL: ' + process.env.WEBCERT_URL + uri);
    return process.env.WEBCERT_URL + uri;
}

function uriTemplate(strings, ...keys) {
    // Applicerar encodeURIComponent på varje variabel i templatet
    return strings.map((s, i) => [s, encodeURIComponent(keys[i])])
        .slice(0, -1)
        .reduce((sum, str) => sum += str[0] + str[1], '');
}
