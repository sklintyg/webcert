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

/*globals protractor, wcTestTools, browser, intyg, logger,person, JSON, Promise */

'use strict';
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var testdataHelpers = wcTestTools.helpers.testdata;
var testdata = wcTestTools.testdata;
var testpatienter = testdata.values.patienter;
var intygURL = helpers.intygURL;
var loginHelpers = require('./inloggning/login.helpers.js');


module.exports = function() {
    this.Given(/^jag trycker på visa intyget$/, function() {
        return element(by.id('showBtn-' + intyg.id)).sendKeys(protractor.Key.SPACE);
    });


    this.Given(/^jag går tillbaka$/, function() {
        return fkUtkastPage.backBtn.sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^(jag går in på utkastet|jag går in på intyget med edit länken)$/, function(arg1) {
        var intygShortcode = helpers.getAbbrev(intyg.typ).toLowerCase();
        var link = '/web/dashboard#/' + intygShortcode + '/edit/' + intyg.id + '/';
        logger.info('Går till ' + link);
        return browser.get(link);
    });

    this.Given(/^ska jag komma till intygssidan$/, function() {
        var intygShortcode = helpers.getAbbrev(intyg.typ).toLowerCase();
        var link = '/web/dashboard#/intyg/' + intygShortcode + '/' + intyg.id;
        return browser.getCurrentUrl().then(function(currentUrl) {
            expect(currentUrl).to.contain(link);
            logger.info('Sida som verifieras: ' + currentUrl);


        });

    });

    this.Given(/^jag väljer vårdenheten "([^"]*)"$/, function(enhetHSA) {
        var enhetSelectorLink = element(by.id('wc-integration-enhet-selector-select-active-unit-' + enhetHSA + '-link'));
        global.user.enhetId = enhetHSA;

        return enhetSelectorLink.click().then(function() {
            return browser.sleep(3000);
        });
        //return element.all(by.cssContainingText('.enhet', ve)).sendKeys(protractor.Key.ENTER);
    });

    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med annat namn$/, function() {
        person.forNamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
        person.efterNamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
    });
    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med annan adress$/, function() {
        person.adress = {
            postadress: 'Västra storgatan 20',
            postort: 'Karlstad',
            postnummer: '66130'

        };
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
    });

    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer$/, function() {
        global.ursprungligPerson = JSON.parse(JSON.stringify(global.person));

        // Ta bort tidigare person så att vi inte råkar välja samma
        var valbaraPatienter = testpatienter.filter(function(el) {
            return el.id !== global.ursprungligPerson.id;
        });
        console.log(testpatienter);
        console.log(valbaraPatienter);

        global.person = testdataHelpers.shuffle(valbaraPatienter)[0];
        logger.info('Går in på personnummer: ' + global.person.id);
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=' + global.person.id);
    });

    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer$/, function() {
        global.ursprungligPerson = JSON.parse(JSON.stringify(global.person));
        global.person.id = '3243342';
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=' + global.person.id);
    });


    this.Given(/^jag går in på (intygsutkastet|intyget)( via djupintegrationslänk| via uthoppslänk)*$/, function(intygstyp, origin) {
        return gotoIntyg(intygstyp, origin);
    });

    function gotoIntyg(intygstyp, origin, addToUrl) {
        var url;
        var usingCreateDraft2;
        if (intyg && intyg.typ) {
            usingCreateDraft2 = helpers.isSMIIntyg(intyg.typ) || helpers.isTSIntyg(intyg.typ);
        }
        if (origin === ' via djupintegrationslänk') {
            if (usingCreateDraft2) {

                if (!person.adress) {
                    person.adress = {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'

                    };
                }
                var intygShortCode = helpers.getAbbrev(intyg.typ);
                intygShortCode = intygShortCode.toLowerCase();
                console.log(intygShortCode);
                url = process.env.WEBCERT_URL + 'visa/intyg/';
                //url += intygShortCode +'/';
                url += global.intyg.id;
                url += '?';
                url += 'fornamn=' + encodeURIComponent(person.forNamn) + '&';
                url += 'efternamn=' + encodeURIComponent(person.efterNamn) + '&';
                url += 'postadress=' + encodeURIComponent(person.adress.postadress) + '&';
                url += 'postnummer=' + encodeURIComponent(person.adress.postnummer) + '&';
                url += 'postort=' + encodeURIComponent(person.adress.postort) + '&';
                url += 'enhet=' + global.user.enhetId + '&';


            } else {
                url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id;
                url = url + '?';
                // url += 'enhet=' + global.user.enhetId + '&';
            }
        } else if (intygstyp === 'intyget' && origin === ' via uthoppslänk') {
            url = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + global.intyg.id + '/questions';

        } else if (intygstyp === 'intyget' && origin === undefined) {
            url = intygURL(intyg.typ, intyg.id);
            /*if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
                url = process.env.WEBCERT_URL + 'web/dashboard#/intyg/luse/' + global.intyg.id;
            } else {
                url = process.env.WEBCERT_URL + 'web/dashboard#/intyg/fk7263/' + global.intyg.id;

            }*/
        } else {
            logger.error('Okänd parameter origin: ' + origin + ', intygstyp: ' + intygstyp);
        }

        if (addToUrl) {
            url += addToUrl;
        }

        var loginIfSessionUsed = function() {
            console.log('Går till url: ' + url);
            if (global.sessionUsed) {
                console.log('Loggar in med tidigare användare..');
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

        };

        return loginIfSessionUsed().then(function() {
            return browser.get(url)
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
                                    return browser.sleep(3000).then(function() { //sleep eftersom vi directas via säkerhetstjänsten
                                        return helpers.fetchMessageIds(intyg.typ);
                                    });
                                });
                            } else {
                                return browser.sleep(3000).then(function() { //sleep eftersom vi directas via säkerhetstjänsten
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
    this.When(/^jag går in på intyget via djupintegrationslänk och har parametern "([^"]*)" satt till "([^"]*)"$/, function(param, paramValue) {
        return gotoIntyg('intyget', ' via djupintegrationslänk', param + '=' + paramValue);

    });

    this.Given(/^jag går till ej signerade utkast$/, function() {
        return element(by.id('menu-unsigned')).click();
    });
    var savedLink;
    this.Given(/^jag sparar länken till aktuell sida$/, function() {
        return browser.getCurrentUrl().then(function(currentUrl) {
            logger.info('Aktuell sida: ' + currentUrl);
            savedLink = currentUrl;
        });
    });

    this.Given(/^går till den sparade länken$/, function() {
        return browser.get(savedLink);
    });

    this.Given(/^jag verifierar att URL:en är samma som den sparade länken$/, function() {
        return browser.getCurrentUrl().then(function(currentUrl) {
            expect(currentUrl).to.equal(savedLink);
            logger.info('Sida som verifieras: ' + currentUrl);

        });
    });

    this.Given(/^jag trycker på knappen med texten "([^"]*)"$/, function(BtnText) {
        return element(by.cssContainingText('.btn', BtnText)).sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^jag trycker på checkboxen med texten "([^"]*)"$/, function(BtnText) {
        return element(by.cssContainingText('label.checkbox', BtnText)).sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^jag anger "([^"]*)" i valet "([^"]*)"$/, function(svar, text) {

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


    this.Given(/^jag går in på healthcheck\-sidan$/, function() {
        browser.ignoreSynchronization = true;
        return browser.get('healthcheck.jsp');
    });

    this.Given(/^ska status för "([^"]*)" vara "([^"]*)"$/, function(checknamn, varaText) {
        var tr = element(by.cssContainingText('tr', checknamn));
        return expect(tr.getText()).to.eventually.contain(varaText);
    });

    this.Given(/^jag byter fokus från fält$/, function() {
        var activeEle = browser.driver.switchTo().activeElement();
        return activeEle.sendKeys(protractor.Key.TAB);
    });



};
