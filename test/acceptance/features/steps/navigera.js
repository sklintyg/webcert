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

/*globals protractor, wcTestTools, browser, intyg, logger,person, JSON, Promise */

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


function gotoIntyg(intygstyp, origin, addToUrl) {
    var usingCreateDraft2;
    if (intyg && intyg.typ) {
        usingCreateDraft2 = helpers.isSMIIntyg(intyg.typ) || helpers.isTSIntyg(intyg.typ);
    }

    if (!person.adress) {
        person.adress = {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        };
    }

    var url = getIntegrationUrl(origin);

    if (addToUrl) {
        url += addToUrl;
    }

    logger.silly('Går till url: ' + url);
    return loginIfSessionUsed().then(function() {
        return browser.get(url)
            .then(function() {
                return helpers.removeAlerts();
            })
            .then(function() {
                return expect(element(by.id('wcHeader')).isPresent()).to.eventually.equal(true);
            }).then(function() {
                helpers.injectConsoleTracing();
                global.sessionUsed = true;
                if (!usingCreateDraft2) { // om djupintegration v1 så kommer det fram uppdragsval
                    var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-' + global.user.enhetId + '-link'));
                    enhetSelectorLink.isPresent().then(function(isPresent) {
                        if (isPresent) {
                            return enhetSelectorLink.click().then(function() {
                                return helpers.pageReloadDelay().then(function() { //sleep eftersom vi directas via säkerhetstjänsten
                                    return helpers.fetchMessageIds(intyg.typ);
                                });
                            });
                        } else {
                            return helpers.pageReloadDelay().then(function() { //sleep eftersom vi directas via säkerhetstjänsten
                                return helpers.fetchMessageIds(intyg.typ);
                            });
                        }

                    });
                } else {
                    return helpers.fetchMessageIds(intyg.typ);
                }
            });
    });
}

function loginIfSessionUsed() {
    if (global.sessionUsed) {
        logger.silly('Loggar in med tidigare användare..');
        return loginHelpers.logInAsUser({
            forNamn: global.user.forNamn,
            efterNamn: global.user.efterNamn,
            hsaId: global.user.hsaId,
            enhetId: global.user.enhetId,
            lakare: global.user.lakare,
            origin: global.user.origin
        });
    } else {
        return Promise.resolve('Använder tidigare session');
    }
}


function getIntegrationUrl(origin) {
    var url;
    var intygUrlShortCode = helpers.getPathShortcode(intyg.typ);

    switch (origin) {
        case ' via djupintegrationslänk':

            intygUrlShortCode = intygUrlShortCode.toLowerCase();
            logger.silly(intygUrlShortCode);
            url = process.env.WEBCERT_URL + 'visa/intyg/';
            //url += intygUrlShortCode + '/';  //Integrerade journalsystem använder inte intygstyp i URL
            url += global.intyg.id;
            url += '?';
            url += 'fornamn=' + encodeURIComponent(person.forNamn) + '&';
            url += 'efternamn=' + encodeURIComponent(person.efterNamn) + '&';
            url += 'postadress=' + encodeURIComponent(person.adress.postadress) + '&';
            url += 'postnummer=' + encodeURIComponent(person.adress.postnummer) + '&';
            url += 'postort=' + encodeURIComponent(person.adress.postort) + '&';
            url += 'enhet=' + global.user.enhetId + '&';
            break;
        case ' utan integrations parametrar':
            intygUrlShortCode = intygUrlShortCode.toLowerCase();
            logger.silly(intygUrlShortCode);
            url = process.env.WEBCERT_URL + 'visa/intyg/';
            //url += intygUrlShortCode + '/'; //Integrerade journalsystem använder inte intygstyp i URL
            url += global.intyg.id;
            break;

        case ' via uthoppslänk':
            url = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + global.intyg.id + '/questions';
            break;
        case undefined:
            url = intygURL(intyg.typ, intyg.id);
            break;
        default:
            url = intygURL(intyg.typ, intyg.id);
            logger.error('Okänd parameter origin: ' + origin);
    }
    return url;

}

/*
 *	Test steg
 *
 */
Given(/^jag trycker på visa intyget$/, function() {
    return element(by.id('showBtn-' + intyg.id)).sendKeys(protractor.Key.SPACE);
});

Given(/^(jag går in på utkastet|jag går in på intyget med edit länken)$/, function(arg1) {
    var intygUrlShortcode = helpers.getPathShortcode(intyg.typ).toLowerCase();
    var link = '/#/' + intygUrlShortcode + '/edit/' + intyg.id + '/';
    logger.info('Går till ' + link);
    return browser.get(link).then(function() {
        return helpers.pageReloadDelay();
    });
});

Given(/^ska jag komma till intygssidan$/, function() {
    var intygUrlShortcode = helpers.getPathShortcode(intyg.typ).toLowerCase();
    var link = '/#/intyg/' + intygUrlShortcode + '/' + intyg.id;
    return browser.getCurrentUrl().then(function(currentUrl) {
        expect(currentUrl).to.contain(link);
        logger.info('Sida som verifieras: ' + currentUrl);
    });
});

Given(/^jag väljer vårdenheten "([^"]*)"$/, function(enhetHSA) {
    var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-' + enhetHSA + '-link'));
    global.user.enhetId = enhetHSA;

    return enhetSelectorLink.click().then(function() {
        return helpers.pageReloadDelay();
    });
    //return element.all(by.cssContainingText('.enhet', ve)).sendKeys(protractor.Key.ENTER);
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med annat namn$/, function() {
    person.forNamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
    person.efterNamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
    return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
});
Given(/^jag går in på intygsutkastet via djupintegrationslänk med annan adress$/, function() {
    person.adress = {
        postadress: 'Västra storgatan 20',
        postort: 'Karlstad',
        postnummer: '66130'

    };
    return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer$/, function() {
    global.ursprungligPerson = JSON.parse(JSON.stringify(global.person));

    // Ta bort tidigare person så att vi inte råkar välja samma
    var valbaraPatienter = testpatienter.filter(function(el) {
        return el.id !== global.ursprungligPerson.id;
    });
    logger.silly(testpatienter);
    logger.silly(valbaraPatienter);

    global.person = testdataHelpers.shuffle(valbaraPatienter)[0];
    logger.info('Går in på personnummer: ' + global.person.id);
    return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=' + global.person.id);
});

Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer$/, function() {
    global.ursprungligPerson = JSON.parse(JSON.stringify(global.person));
    global.person.id = '3243342';
    return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=' + global.person.id);
});


Given(/jag försöker gå in på intygsutkastet via djupintegrationslänk$/, function() {
    //"Försöker gå in" är inte samma steg som "går in". p.g.a. expect logiken.
    return loginIfSessionUsed().then(function() {
        return browser.get(getIntegrationUrl(' via djupintegrationslänk'));
    }).then(function() {
        return browser.sleep(2000);
    });
});

Given(/^jag går in på (intygsutkastet|intyget)( via djupintegrationslänk| via uthoppslänk| utan integrations parametrar)*$/, function(intygstyp, origin) {
    return gotoIntyg(intygstyp, origin);
});



When(/^jag går in på intyget via djupintegrationslänk och har parametern "([^"]*)" satt till "([^"]*)"$/, function(param, paramValue) {
    return gotoIntyg('intyget', ' via djupintegrationslänk', param + '=' + paramValue);

});

Given(/^jag går till ej signerade utkast$/, function() {
    return element(by.id('menu-unsigned')).click();
});
var savedLink;
Given(/^jag sparar länken till aktuell sida$/, function() {
    return browser.getCurrentUrl().then(function(currentUrl) {
        logger.info('Aktuell sida: ' + currentUrl);
        savedLink = currentUrl;
    });
});

Given(/^går till den sparade länken$/, function() {
    return browser.get(savedLink);
});

Given(/^jag verifierar att URL:en är samma som den sparade länken$/, function() {
    return browser.getCurrentUrl().then(function(currentUrl) {
        expect(currentUrl).to.equal(savedLink);
        logger.info('Sida som verifieras: ' + currentUrl);

    });
});

Given(/^jag trycker på knappen med texten "([^"]*)"$/, function(BtnText) {
    return element(by.cssContainingText('.btn', BtnText)).sendKeys(protractor.Key.SPACE);
});

Given(/^jag trycker på checkboxen med texten "([^"]*)"$/, function(BtnText) {
    return element(by.cssContainingText('label.checkbox', BtnText)).sendKeys(protractor.Key.SPACE);
});

Given(/^jag anger "([^"]*)" i valet "([^"]*)"$/, function(svar, text) {

    var tr = element(by.cssContainingText('tr', text));

    if (svar === 'Ja') {
        return tr.all(by.css('td')).get(0).element(by.css('input')).sendKeys(protractor.Key.SPACE);
    } else if (svar === 'Typ 2') {
        var typVal = tr.all(by.css('td')).get(2).all(by.css('input')).get(1);
        return typVal.sendKeys(protractor.Key.SPACE);
    } else {
        return tr.all(by.css('td')).get(1).element(by.css('input')).sendKeys(protractor.Key.SPACE);
    }
});


Given(/^jag går in på healthcheck\-sidan$/, function() {
    browser.ignoreSynchronization = true;
    return browser.get('healthcheck.jsp');
});

Given(/^ska status för "([^"]*)" vara "([^"]*)"$/, function(checknamn, varaText) {
    var tr = element(by.cssContainingText('tr', checknamn));
    return expect(tr.getText()).to.eventually.contain(varaText);
});

Given(/^jag byter fokus från fält$/, function() {
    var activeEle = browser.driver.switchTo().activeElement();
    return activeEle.sendKeys(protractor.Key.TAB);
});
