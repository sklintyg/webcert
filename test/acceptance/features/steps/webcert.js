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

/* global pages, intyg, browser, protractor, Promise, logger */

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


var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;

var testTools = require('common-testtools');
testTools.protractorHelpers.init();


/*
 *	Stödfunktioner
 *
 */

function textContainsAnyOfValues(text, checkValues) {

    for (var i = 0; i < checkValues.length; i++) {
        if (text.includes(checkValues[i])) {
            return true;
        }
    }
    return false;
}

function checkElementsForText(els, checkValues) {
    return els.map(function(elm, index) {
        return elm.getText();
    }).then(function(texts) {

        texts.forEach(function(val, index) {

            if (index > 0) { //Inga checkar på tabell-header
                logger.silly('Kontrollerar rad: ' + val);
                var hasFound = textContainsAnyOfValues(val, checkValues);
                if (!hasFound) {
                    throw ('Hittade inte ' + checkValues.join(' eller ') + ' i ' + val);
                }
            }
        });
        return Promise.resolve('Hittade texter i alla element');

    });
}

/*
 *	Test steg
 *
 */

Given(/^jag går till Sök\/skriv intyg$/, function() {
    return element(by.id('menu-skrivintyg')).typeKeys(protractor.Key.SPACE);
});

Given(/^ska intyget visa varningen "([^"]*)"$/, function(arg1, callback) {
    expect(element(by.id('certificate-is-revoked-message-text')).getText())
        .to.eventually.contain(arg1).and.notify(callback);
});

Given(/^ska intyget inte finnas i intygsöversikten$/, function(callback) {
    element(by.id('intygFilterSamtliga')).sendKeys(protractor.Key.SPACE);
    expect(element(by.id('showBtn-' + intyg.id)).isPresent()).to.become(false).and.notify(callback);
});

Given(/^det finns ett "([^"]*)"$/, function(intygtyp) {
    return element(by.id('prevIntygTable')).getText().then(function(text) {
        if (text.indexOf(intygtyp) >= 0) {
            return Promise.resolve('Intyg finns');
        } else {
            return browser.getCurrentUrl().then(function(currentUrl) {
                return sokSkrivIntygUtkastTypePage.selectIntygTypeByLabel(intygtyp).then(function() {
                    return sokSkrivIntygUtkastTypePage.intygTypeButton.sendKeys(protractor.Key.SPACE).then(function() {
                        return browser.get(currentUrl);
                    });
                });
            });
        }
    });
});



Given(/^ska jag inte se intyg av annan typ än "([^"]*)"$/, function(typer) {
    typer = typer.split(',');
    var els = element(by.id('prevIntygTable')).all(by.css('tr'));
    return checkElementsForText(els, typer);
});

Given(/^ska jag inte se utkast av annan typ än "([^"]*)"$/, function(typer) {
    typer = typer.split(',');
    var els = element(by.id('unsignedCertTable')).all(by.css('tr'));
    return checkElementsForText(els, typer);
});

Given(/^jag ska endast ha möjlighet att skapa nya "([^"]*)" utkast$/, function(typer) {
    typer = typer.split(',');
    var els = element(by.id('intygType')).all(by.css('option'));
    return checkElementsForText(els, typer);
});
