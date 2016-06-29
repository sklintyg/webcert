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

/*global browser, intyg, logger, person, protractor, pages, JSON */
'use strict';

var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var createIntygWithStatus = require('./helpers_create_intyg.js').createIntygWithStatus;
var getIntygElementRow = require('./helpers.js').getIntygElementRow;

function gotoIntyg(intygstyp, status, intygRadElement, cb) {

    //Om det inte finns några intyg att använda
    if (!intygRadElement) {
        logger.info('Hittade inget intyg, skapar ett nytt..');
        createIntygWithStatus(intygstyp, status, function(err) {
            if (err) {
                cb(err);
            }
            //Uppdatera sidan och gå in på patienten igen
            browser.refresh();
            browser.get('/web/dashboard#/create/choose-patient/index');

            sokSkrivIntygPage.selectPersonnummer(person.id);

            getIntygElementRow(intygstyp, status, function(el) {
                el.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE);
                cb();
            });
        });
    }
    //Gå in på intyg
    else {
        intygRadElement.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE);
        cb();
    }
}
module.exports = function() {

    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, {
        timeout: 500 * 1000
    }, function(intygstyp, status, callback) {
        getIntygElementRow(intygstyp, status, function(el) {
            gotoIntyg(intygstyp, status, el, function(err) {
                browser.getCurrentUrl().then(function(text) {
                    intyg.id = text.split('/').slice(-1)[0];
                    intyg.id = intyg.id.split('?')[0];
                    logger.info('intyg.id:' + intyg.id);
                    if (err) {
                        callback(JSON.stringify(err));
                    } else {
                        callback();
                    }
                });
            });
        });
    });
};
