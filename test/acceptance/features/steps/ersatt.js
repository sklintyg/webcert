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

/* globals protractor, intyg, browser, Promise, logger */

'use strict';

function intygURL(typAvIntyg, intygId) {
    if (typAvIntyg === 'Läkarutlåtande för sjukersättning') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/luse/' + global.intyg.id);
    } else if (typAvIntyg === 'Läkarintyg för sjukpenning') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/lisjp/' + global.intyg.id);
    } else if (typAvIntyg === 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/luae_fs/' + global.intyg.id);
    } else if (typAvIntyg === 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/luae_na/' + global.intyg.id);
    } else if (typAvIntyg === 'Läkarintyg FK 7263') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/fk7263/' + global.intyg.id);
    } else if (typAvIntyg === 'Transportstyrelsens läkarintyg, diabetes') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/ts-diabetes/' + global.intyg.id);
    } else if (typAvIntyg === 'Transportstyrelsens läkarintyg') {
        return Promise.resolve(process.env.WEBCERT_URL + 'web/dashboard#/intyg/ts-bas/' + global.intyg.id);
    }
}

module.exports = function() {

    this.Given(/^ska jag se en knapp med texten "([^"]*)"$/, function(btnTxt) {
        return expect(element(by.id('ersattBtn')).getText()).to.eventually.equal(btnTxt);
    });

    this.Given(/^klickar på ersätta knappen$/, function() {
        return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE);
    });

    this.Given(/^klickar på ersätta knappen och ersätter intyget$/, function() {
        global.ersattintyg = {};
        global.ersattintyg.id = intyg.id;
        global.ersattintyg.typ = intyg.typ;

        return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE).then(function() {
            return element(by.id('button1ersatt-dialog')).sendKeys(protractor.Key.SPACE).then(function() {
                logger.info('Clicked ersätt button');
            });
        });
    });

    this.Given(/^gå tillbaka till det ersatta intyget$/, function() {
        return browser.sleep(4000).then(function() {
            return intygURL(global.ersattintyg.typ, global.ersattintyg.id).then(function(url) {
                return browser.get(url).then(function() {
                    logger.info('Går till url: ' + url);
                });
            });
        });
    });

    this.Given(/^ska jag se en texten "([^"]*)" som innehåller en länk till det ersatta intyget$/, function(replacedMessage) {
        return expect(element(by.id('wc-intyg-replaced-message')).getText()).to.eventually.contain(replacedMessage);
    });

    this.Given(/^ska meddelandet som visas innehålla texten "([^"]*)"$/, function(modalMsg) {
        return expect(element(by.css('.modal-body')).getText()).to.eventually.contain(modalMsg);
    });

};
