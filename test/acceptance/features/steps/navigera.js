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

/*globals protractor, wcTestTools, browser, logger, JSON, Promise */

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
var testdataHelpers = wcTestTools.helpers.testdata;
var testdata = wcTestTools.testdata;
var testpatienter = testdata.values.patienter;
var intygURL = helpers.intygURL;
var loginHelpers = require('./inloggning/login.helpers.js');


/*
 *	Stödfunktioner
 *
 */


function gotoIntyg(user, intyg, patient, origin, addToUrl) {
    var usingCreateDraft2;
    if (intyg && intyg.typ) {
        usingCreateDraft2 = helpers.isSMIIntyg(intyg.typ) || helpers.isTSIntyg(intyg.typ);
    }

    if (!patient.adress) {
        patient.adress = {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        };
    }

    var url = getIntegrationUrl(user, origin, intyg, patient);

    if (addToUrl) {
        url += addToUrl;
    }

    logger.silly('Går till url: ' + url);
    return loginIfSessionUsed(user).then(function() {
        return helpers.getUrl(url)
            .then(function() {
                return expect(element(by.id('wcHeader')).isPresent()).to.eventually.equal(true);
            }).then(function() {
                helpers.injectConsoleTracing();
                global.sessionUsed = true;
                if (!usingCreateDraft2) { // om djupintegration v1 så kommer det fram uppdragsval
                    var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-' + user.enhetId + '-link'));
                    return enhetSelectorLink.isPresent().then(function(isPresent) {
                        if (isPresent) {
                            return enhetSelectorLink.click().then(function() {
                                return helpers.pageReloadDelay(); //sleep eftersom vi directas via säkerhetstjänsten
                            });
                        } else {
                            return helpers.pageReloadDelay();
                        }

                    });
                }
                return;
            });
    });
}

function loginIfSessionUsed(user) {
    if (global.sessionUsed) {
        logger.silly('Loggar in med tidigare användare..');
        return loginHelpers.logInAsUser({
            forNamn: user.forNamn,
            efterNamn: user.efterNamn,
            hsaId: user.hsaId,
            enhetId: user.enhetId,
            lakare: user.lakare,
            origin: user.origin
        });
    } else {
        logger.silly('Använder tidigare session');
        return Promise.resolve('Använder tidigare session');
    }
}


function getIntegrationUrl(user, origin, intyg, patient) {
    var url;
    var intygUrlShortCode = helpers.getInternShortcode(intyg.typ);

    switch (origin) {
        case ' via djupintegrationslänk':

            intygUrlShortCode = intygUrlShortCode.toLowerCase();
            logger.silly(intygUrlShortCode);
            url = process.env.WEBCERT_URL + 'visa/intyg/';
            //url += intygUrlShortCode + '/';  //Integrerade journalsystem använder inte intygstyp i URL
            url += intyg.id;
            url += '?';
            url += 'fornamn=' + encodeURIComponent(patient.forNamn) + '&';
            url += 'efternamn=' + encodeURIComponent(patient.efterNamn) + '&';
            url += 'postadress=' + encodeURIComponent(patient.adress.postadress) + '&';
            url += 'postnummer=' + encodeURIComponent(patient.adress.postnummer) + '&';
            url += 'postort=' + encodeURIComponent(patient.adress.postort) + '&';
            url += 'enhet=' + user.enhetId + '&';
            break;
        case ' utan integrations parametrar':
            intygUrlShortCode = intygUrlShortCode.toLowerCase();
            logger.silly(intygUrlShortCode);
            url = process.env.WEBCERT_URL + 'visa/intyg/';
            //url += intygUrlShortCode + '/'; //Integrerade journalsystem använder inte intygstyp i URL
            url += intyg.id;
            break;

        case ' via uthoppslänk':
            url = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + intyg.id + '/questions';
            break;
        case undefined:
            url = intygURL(intyg);
            break;
        default:
            url = intygURL(intyg);
            logger.error('Okänd parameter origin: ' + origin);
    }
    return url;

}

/*
 *	Test steg
 *
 */
Given(/^jag uppdaterar sidan$/, function() {
    return browser.refresh().then(function() {
        return helpers.pageReloadDelay();
    });
});

Given(/^jag trycker på visa intyget$/, function() {
    return element(by.id('showBtn-' + this.intyg.id)).sendKeys(protractor.Key.SPACE);
});

Given(/^(jag går in på utkastet|jag går in på intyget med edit länken)$/, function(arg1) {
    var intygUrlShortcode = helpers.getInternShortcode(this.intyg.typ).toLowerCase();
    var link = '/#/' + intygUrlShortcode + '/edit/' + this.intyg.id + '/';
    return helpers.getUrl(link).then(function() {
        return helpers.pageReloadDelay();
    });
});

Given(/^ska jag komma till intygssidan$/, function() {
    var intygUrlShortcode = helpers.getInternShortcode(this.intyg.typ).toLowerCase();
    var link = '/#/intyg/' + intygUrlShortcode + '/' + this.intyg.id;
    return browser.getCurrentUrl().then(function(currentUrl) {
        expect(currentUrl).to.contain(link);
        logger.info('Sida som verifieras: ' + currentUrl);
    });
});

Given(/^jag väljer vårdenheten "([^"]*)"$/, function(enhetHSA) {
    var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-' + enhetHSA + '-link'));
    this.user.enhetId = enhetHSA;

    return enhetSelectorLink.click().then(function() {
        return helpers.pageReloadDelay();
    });
    //return element.all(by.cssContainingText('.enhet', ve)).sendKeys(protractor.Key.ENTER);
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med annat namn$/, function() {
    this.patient.forNamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
    this.patient.efterNamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
    return gotoIntyg(this.user, this.intyg, this.patient, ' via djupintegrationslänk');
});
Given(/^jag går in på intygsutkastet via djupintegrationslänk med annan adress$/, function() {
    this.patient.adress = {
        postadress: 'Västra storgatan 20',
        postort: 'Karlstad',
        postnummer: '66130'

    };
    return gotoIntyg(this.user, this.intyg, this.patient, ' via djupintegrationslänk');
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer$/, function() {
    global.ursprungligPerson = JSON.parse(JSON.stringify(this.patient));

    // Ta bort tidigare person så att vi inte råkar välja samma
    var valbaraPatienter = testpatienter.filter(function(el) {
        return el.id !== global.ursprungligPerson.id;
    });
    logger.silly(testpatienter);
    logger.silly(valbaraPatienter);

    this.patient = testdataHelpers.shuffle(valbaraPatienter)[0];
    logger.info('Går in på personnummer: ' + this.patient.id);
    return gotoIntyg(this.user, this.intyg, this.patient, ' via djupintegrationslänk', 'alternatePatientSSn=' + this.patient.id);
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer$/, function() {
    global.ursprungligPerson = JSON.parse(JSON.stringify(this.patient));
    this.patient.id = '3243342';
    return gotoIntyg(this.user, this.intyg, this.patient, ' via djupintegrationslänk', 'alternatePatientSSn=' + this.patient.id);
});


Given(/jag försöker gå in på intygsutkastet via djupintegrationslänk$/, function() {
    //"Försöker gå in" är inte samma steg som "går in". p.g.a. expect logiken.
    let user = this.user;
    return loginIfSessionUsed(user).then(function() {
        return helpers.getUrl(getIntegrationUrl(user, ' via djupintegrationslänk', this.intyg));
    });
});
Given(/jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "([^"]*)"$/, function(param) {
    //"Försöker gå in" är inte samma steg som "går in". p.g.a. expect logiken.
    let user = this.user;
    return loginIfSessionUsed(user).then(function() {
        let url = getIntegrationUrl(user, ' via djupintegrationslänk', this.intyg, this.patient);
        url += param;
        return helpers.getUrl(url);
    });
});

Given(/^jag går in på (intygsutkastet|intyget)( via djupintegrationslänk| via uthoppslänk| utan integrations parametrar)*$/, function(intygstyp, origin) {
    return gotoIntyg(this.user, this.intyg, this.patient, origin);
});

Given(/^jag trycker på knappen med texten "([^"]*)"$/, function(BtnText) {
    return element(by.cssContainingText('.btn', BtnText)).sendKeys(protractor.Key.SPACE);
});

When(/^jag går in på intyget via djupintegrationslänk med parameter "([^"]*)"$/, function(param) {
    return gotoIntyg(this.user, this.intyg, this.patient, ' via djupintegrationslänk', param);
});

Given(/^jag går till ej signerade utkast$/, function() {
    return element(by.id('menu-unsigned')).click().then(function() {
        helpers.pageReloadDelay();
    });
});
var savedLink;
Given(/^jag sparar länken till aktuell sida$/, function() {
    return browser.getCurrentUrl().then(function(currentUrl) {
        logger.info('Aktuell sida: ' + currentUrl);
        savedLink = currentUrl;
    });
});

Given(/^går till den sparade länken$/, function() {
    return helpers.getUrl(savedLink);
});

Given(/^jag verifierar att URL:en är samma som den sparade länken$/, function() {
    return browser.getCurrentUrl().then(function(currentUrl) {
        expect(currentUrl).to.equal(savedLink);
        logger.info('Sida som verifieras: ' + currentUrl);

    });
});

Given(/^jag går in på healthcheck\-sidan$/, function() {
    browser.ignoreSynchronization = true;
    return helpers.getUrl('healthcheck.jsp');
});

Given(/^ska status för "([^"]*)" vara "([^"]*)"$/, function(checknamn, varaText) {
    var tr = element(by.cssContainingText('tr', checknamn));
    return expect(tr.getText()).to.eventually.contain(varaText);
});

Given(/^jag byter fokus från fält$/, function() {
    var activeEle = browser.driver.switchTo().activeElement();
    return activeEle.sendKeys(protractor.Key.TAB);
});
