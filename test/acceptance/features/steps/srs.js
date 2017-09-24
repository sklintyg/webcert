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
        createDraftUsingSOAP(user, srsdata.patient.id)
        .then(intygsId => browser.get(buildLinkToIntyg(intygsId, srsdata.patient, user.enhetId)))
        .then(() => browser.waitForAngular())
        .then(() => browser.sleep(2000)) // Behövs för att waitForAngular tydligen inte räcker
        .then(() => expect(element(by.id('wcHeader')).isPresent()).to.eventually.equal(true))
        .then(() => setConsent(srsdata.patient, user, samtycke))
    );

    this.Then(/^ska en frågepanel för SRS "(inte)? ?visas"$/,
        panelStatus => expect(fk7263utkast.srs.panel().isDisplayed()).to.eventually.equal(panelStatus !== 'inte')
    );

    this.Then(/^ska en pil med texten "(Visa mindre|Visa mer)" visas$/,
        text => expect(findLabelContainingText(text).isPresent()).to.eventually.equal(true)
    );

    this.When(/^jag (?:fyller|fyllt) i diagnoskod som "(finns i SRS|inte finns i SRS)"$/,
        srsStatus => fk7263utkast.angeDiagnosKod(srsdata.diagnoskoder[srsStatus])
    );

    this.Then(/^ska knappen för SRS vara i läge "(stängd|öppen|gömd)"$/,
        srsButtonStatus => expect(fk7263utkast.getSRSButtonStatus()).to.eventually.equal(srsButtonStatus)
    );

    this.When(/^jag klickar på knappen för SRS$/,
        () => fk7263utkast.srs.knapp().click()
    );

    this.When(/^jag klickar på pilen$/,
        () => fk7263utkast.srs.visamer().click()
        .then(() => browser.sleep(500)) // Det tar en stund för panelen att maximeras/minimeras
    );

    this.Then(/^ska frågepanelen för SRS vara "(minimerad|maximerad)"$/,
        status => expect(fk7263utkast.getSRSQuestionnaireStatus()).to.eventually.equal(status)
    );

    this.Then(/^ska en fråga om samtycke visas$/,
        () => expect(
            findLabelContainingText('Patienten samtycker till att delta').isPresent()
        ).to.eventually.equal(true)
    );

    this.When(/^jag anger att patienten (inte)? ?samtycker till SRS$/,
        samtycke => fk7263utkast.setSRSConsent(samtycke === 'inte' ? false : true)
    );

    this.Then(/^frågan om samtycke ska vara förifylld med "(Ja|Nej)"$/,
        samtycke => expect(fk7263utkast.srs.samtycke[samtycke.toLowerCase()]().isSelected()).to.eventually.equal(true)
    );


    this.Then(/^ska åtgärdsförslag från SRS-tjänsten visas$/,
        () => expect(fk7263utkast.srs.atgarder().isDisplayed()).to.eventually.equal(true)
    );



};

function setConsent(patient, user, consent) {
    /**
     * Injicerar ett skript i browsern som skickar "SetConsent" till webcert backend.
     * Används för att försätta en patient i känt state inför test.
     */
    const patientId = patient.id.slice(0, 8) + '-' + patient.id.slice(8 + 0);
    const link = buildLinkToSetConsent(patientId, user.enhetId);
    return browser.executeAsyncScript(function(url, samtycke) {
            var callback = arguments[arguments.length - 1];
            var xhr = new XMLHttpRequest();
            xhr.open('PUT', url, true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    callback(xhr.responseText);
                }
            };
            xhr.send(samtycke === 'har givit samtycke' ? 'true' : 'false');
        }, link, consent)
        .then(response => console.log('SetConsent respons: ' + response));
}

function buildLinkToSetConsent(patientId, enhetId) {
    let uri = uriTemplate `api/srs/consent/${patientId}/${enhetId}`;
    console.log('Consent URL: ' + process.env.WEBCERT_URL + uri);
    return process.env.WEBCERT_URL + uri;
}

function findLabelContainingText(text) {
    return fk7263utkast.srs.panel()
        .all(by.tagName('div'))
        .filter(ele => ele.getText().then(t => t.includes(text))).first();
}

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
