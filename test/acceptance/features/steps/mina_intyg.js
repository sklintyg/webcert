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

/* globals browser, logger, protractor */

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


var miCheckValues = require('./checkValues/minaintyg');
var STATUS_REGEX;
var helpers = require('./helpers');


/*
 *	Stödfunktioner
 *
 */


function matchString(element) {
    return element.match(STATUS_REGEX);
}

function stringToArray(text) {
    return text.split(/\n/g);
}

/*
 *	Test steg
 *
 */

Given(/^ska intyget( inte)? finnas i Mina intyg$/, function(inte) {
    var skaFinnas = typeof(inte) === 'undefined';
    var intygElement = element(by.id('certificate-' + this.intyg.id));
    return expect(intygElement.isPresent()).to.eventually.equal(skaFinnas).then(function(value) {
        logger.info('OK - skaFinnas=' + skaFinnas + ':' + value);
    }, function(reason) {
        throw ('FEL,Expected skaFinnas=' + skaFinnas + ' Reason:' + reason);
    });
});

Given(/^jag går till Mina intyg för patienten$/, function() {
    browser.ignoreSynchronization = true;
    return helpers.getUrl(process.env.MINAINTYG_URL + '/web/sso?guid=' + this.patient.id).then(function() {
        // Om samtyckesruta visas
        return element(by.id('consentTerms')).isPresent();
    }).then(function(present) {
        if (present) {
            logger.info('Lämnar samtycke..');
            return element(by.id('giveConsentCheckbox')).sendKeys(protractor.Key.SPACE)
                .then(function() {
                    browser.ignoreSynchronization = false;
                    return helpers.largeDelay();
                }).then(function() {
                    element(by.id('giveConsentButton')).sendKeys(protractor.Key.SPACE);
                    browser.ignoreSynchronization = false;
                    return helpers.largeDelay();
                });
        } else {
            browser.ignoreSynchronization = false;
            return;
        }
    });
});

Given(/^ska intygets status i Mina intyg visa "([^"]*)"$/, function(status) {
    // STATUS_REGEX = status.replace(/(\{).+?(\})/g, '(.*)');
    STATUS_REGEX = status;
    var intygElement = element(by.id('certificate-' + this.intyg.id));
    return intygElement.getText().then(function(text) {
        text = stringToArray(text);
        var match = text.find(matchString);
        return expect(match).to.be.ok;
    });
});

Given(/^jag går in på intyget i Mina intyg$/, function(callback) {
    element(by.id('viewCertificateBtn-' + this.intyg.id)).sendKeys(protractor.Key.SPACE).then(callback());
});

Given(/^ska intygets information i Mina intyg vara den jag angett$/, function(callback) {
    if (this.intyg.typ === 'Läkarutlåtande för sjukersättning') {
        miCheckValues.fk.LUSE(this.intyg).then(function(value) {
            logger.info('Alla kontroller utförda OK');
            callback();
        }, function(reason) {
            callback(reason);
        });
    } else {
        callback.pending();
    }
});

Given(/^jag loggar ut ur Mina intyg$/, function() {
    return element(by.id('mvklogoutLink')).sendKeys(protractor.Key.SPACE).then(function() {
        return helpers.hugeDelay();
    });

});
