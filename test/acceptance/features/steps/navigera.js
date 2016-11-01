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

/*globals protractor, wcTestTools, browser, intyg, logger */

'use strict';
var fkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var helpers = require('./helpers');
var testdataHelpers = wcTestTools.helpers.testdata;

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
        return gotoIntyg('intygsutkastet', ' via djupintegrationslänk');
    });

    this.Given(/^jag går in på (intygsutkastet|intyget)( via djupintegrationslänk| via uthoppslänk)*$/, function(intygstyp, origin) {
        return gotoIntyg(intygstyp, origin);
    });

    function gotoIntyg(intygstyp, origin) {
        var url;
        var isSMIIntyg;
        if (intyg && intyg.typ) {
            isSMIIntyg = helpers.isSMIIntyg(intyg.typ);
        }
        if (intygstyp === 'intygsutkastet' && origin === ' via djupintegrationslänk') {

            var fnamn = testdataHelpers.shuffle(['Anna', 'Torsten', 'Anton', 'Jonas', 'Nisse', 'Sture'])[0];
            var enamn = testdataHelpers.shuffle(['Andersson', 'Svensson', 'Klint', 'Ingves', 'Persson'])[0];
            if (isSMIIntyg) {
                url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id;
                url = url + '?';
                url += 'fornamn=' + fnamn + '&';
                url += 'efternamn=' + enamn + '&';
                url += 'postadress=Langgatan%2012&';
                url += 'postnummer=990%2090&';
                url += 'postort=Simrishamn&';
                url += 'ref=testref&';
                url += 'enhet=' + global.user.enhetId;

            } else {
                //EN WORKAROUND med parameter TILLS INTYG 2711 är LÖST
                url = process.env.WEBCERT_URL + 'visa/intyg/' + global.intyg.id + '?fornamn=TODO';
            }
        } else if (intygstyp === 'intyget' && origin === ' via uthoppslänk') {
            url = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + global.intyg.id + '/questions';

        } else if (intygstyp === 'intyget' && origin === undefined) {
            if (intyg.typ === 'Läkarutlåtande för sjukersättning') {
                url = process.env.WEBCERT_URL + 'web/dashboard#/intyg/luse/' + global.intyg.id;
            } else {
                url = process.env.WEBCERT_URL + 'web/dashboard#/intyg/fk7263/' + global.intyg.id;

            }
        } else {
            logger.error('Okänd parameter origin: ' + origin + ', intygstyp: ' + intygstyp);
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
};
