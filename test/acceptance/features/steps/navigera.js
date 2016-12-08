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

/*globals protractor, wcTestTools, browser, intyg, logger,person */

'use strict';
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var testdataHelpers = wcTestTools.helpers.testdata;
var testdata = wcTestTools.testdata;
var testpatienter = testdata.values.patienter;

module.exports = function() {

    this.Given(/^jag går tillbaka$/, function() {
        return fkUtkastPage.backBtn.sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^jag går in på utkastet$/, function() {
        var intygShortcode = helpers.getAbbrev(intyg.typ).toLowerCase();
        var link = '/web/dashboard#/' + intygShortcode + '/edit/' + intyg.id;
        logger.info('Går till ' + link);
        return browser.get(link);
    });

    this.Given(/^jag ändrar enhet till "([^"]*)"$/, function(enhet) {
        return (global.user.enhetId = enhet);
    });

    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med annat namn och adress$/, function() {
        person.fornamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
        person.efternamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
        person.adress = {
            postadress: 'Västra storgatan 20',
            postort: 'Karlstad',
            postnummer: '66130'

        };
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
    });
    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med annat namn$/, function() {
        person.fornamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
        person.efternamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
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
        global.person = testdataHelpers.shuffle(testpatienter)[0];
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=' + global.person.id);
    });

    this.Given(/^jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer$/, function() {
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', 'alternatePatientSSn=3243342');
    });


    this.Given(/^jag går in på (intygsutkastet|intyget)( via djupintegrationslänk| via uthoppslänk)*$/, function(intygstyp, origin) {
        return gotoIntyg(intygstyp, origin);
    });

    function intygURL(typAvIntyg) {
        if (typAvIntyg === 'Läkarutlåtande för sjukersättning') {
            return process.env.WEBCERT_URL + 'web/dashboard#/intyg/luse/' + global.intyg.id;
        } else if (typAvIntyg === 'Läkarintyg för sjukpenning') {
            return process.env.WEBCERT_URL + 'web/dashboard#/intyg/lisjp/' + global.intyg.id;
        } else if (typAvIntyg === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
            return process.env.WEBCERT_URL + 'web/dashboard#/intyg/luae_fs/' + global.intyg.id;
        } else if (typAvIntyg === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
            return process.env.WEBCERT_URL + 'web/dashboard#/intyg/luae_na/' + global.intyg.id;
        } else {
            return process.env.WEBCERT_URL + 'web/dashboard#/intyg/fk7263/' + global.intyg.id;

        }
    }

    function gotoIntyg(intygstyp, origin, addToUrl) {
        var url;
        var isSMIIntyg;
        if (intyg && intyg.typ) {
            isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        }
        if (intygstyp === 'intygsutkastet' && origin === ' via djupintegrationslänk') {
            if (isSMIIntyg) {
                url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id;
                url = url + '?';
                url += 'fornamn=' + encodeURIComponent(person.fornamn) + '&';
                url += 'efternamn=' + encodeURIComponent(person.efternamn) + '&';
                url += 'postadress=' + encodeURIComponent(person.adress.postadress) + '&';
                url += 'postnummer=' + encodeURIComponent(person.adress.postnummer) + '&';
                url += 'postort=' + encodeURIComponent(person.adress.postort) + '&';
                url += 'ref=testref&';
                url += 'enhet=' + global.user.enhetId + '&';

            } else {
                //EN WORKAROUND med parameter TILLS INTYG 2711 är LÖST
                url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id + '?fornamn=TODO';
            }
        } else if (intygstyp === 'intyget' && origin === ' via uthoppslänk') {
            url = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + global.intyg.id + '/questions';

        } else if (intygstyp === 'intyget' && origin === undefined) {
            url = intygURL(intyg.typ);
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

        return browser.get(url).then(function() {
            console.log('Går till url: ' + url);
            if (!isSMIIntyg) { // om djupintegration v1 så kommer det fram uppdragsval
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
    }


    this.Given(/^ska jag gå in på intyget med en extra "([^"]*)" parametrar med värdet "([^"]*)"$/, function(param, paramValue) {
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk', param + '=' + paramValue);

    });

    this.Given(/^jag går till ej signerade utkast$/, function() {
        return element(by.id('menu-unsigned')).click();
    });
    var savedLink;
    this.Given(/^sparar länken till aktuell sida$/, function() {
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
};
