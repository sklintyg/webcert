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

/* globals browser, intyg, logger, Promise, person */

'use strict';

var miCheckValues = require('./checkValues/minaintyg')


module.exports = function() {

    this.Given(/^ska intyget finnas i Mina intyg$/, function(callback) {
        var intygElement = element(by.id('certificate-' + intyg.id));
        expect(intygElement.isPresent()).to.eventually.equal(true).then(function(value) {
            logger.info('OK - Intyget visas i mina intyg = ' + value);
        }, function(reason) {
            callback('FEL, Intyget visas inte i mina intyg,' + reason);
        })
            .then(callback);
    });

    this.Given(/^jag går till Mina intyg för patienten "([^"]*)"$/, function(pnr, callback) {
        browser.ignoreSynchronization = true;
        browser.get(process.env.MINAINTYG_URL + '/welcome.jsp');
        element(by.id('guid')).sendKeys(pnr);
        element(by.css('input.btn')).click().then(function() {

            // Detta behövs pga att Mina intyg är en extern sida
            browser.sleep(2000);

            // Om samtyckesruta visas
            element(by.id('consentTerms')).isPresent().then(function(result) {
                if (result) {
                    logger.info('Lämnar samtycke..');
                    element(by.id('giveConsentButton')).click()
                        .then(function() {
                            return browser.sleep(3000);
                        })
                        .then(callback);
                } else {
                    browser.ignoreSynchronization = false;
                    callback();
                }
            });
        });
    });

    this.Given(/^ska intygets status i Mina intyg visa "([^"]*)"$/, function(status, callback) {
        var intygElement = element(by.id('certificate-' + intyg.id));
        expect(intygElement.getText()).to.eventually.contain(status).and.notify(callback);
    });

    this.Given(/^jag går in på intyget i Mina intyg$/, function(callback) {
        element(by.id('viewCertificateBtn-' + intyg.id)).click().then(callback());
    });

    this.Given(/^ska intygets information i Mina intyg vara den jag angett$/, function(callback) {
        if(intyg.typ === 'Läkarutlåtande för sjukersättning'){
          miCheckValues.fk.LUSE(intyg).then(function(value) {
            logger.info('Alla kontroller utförda OK');
            callback();
        }, function(reason) {
            callback(reason);
        });  
        }
        else{
            callback.pending();
        }

        
    });
};