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

/* globals protractor*/
'use strict';
module.exports = function () {

    this.Given(/^går in på Sök\/skriv intyg$/, function (callback) {
        element(by.id('menu-skrivintyg')).click().then(callback);
    });

    this.Given(/^går in på Ej signerade utkast$/, function (callback) {
        element(by.id('menu-unsigned')).click().then(callback);
    });
    
    this.Given(/^är kopieraknappen tillgänglig$/, function (callback) {
	    element(by.id('copyBtn')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^Vidarebeforda knappen synns$/, function (callback) {
	  element(by.xpath('//*[@id=\"unsignedCertTable\"]/table/tbody/tr[2]/td[2]/button')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^avbryter jag vidarebefodran$/, function (callback) {
	  element(by.id('buttonNo')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^ska intyget vara markerat som vidarebefodrad$/, function (callback) {
	  // Write code here that turns the phrase above into concrete actions
	  callback('TBI!');
	});

    this.Given(/^är signeraknappen tillgänglig$/, function(callback) {
        expect(element(by.id('signera-utkast-button')).isPresent()).to.eventually.be.ok.then(function(value) {
            logg('Signeringsknapp existerar ' + value);
        }, function(reason) {
            callback('FEL, Signeringsknapp finns inte på sidan,' + reason);
        });
        expect(element(by.id('signera-utkast-button')).isEnabled()).to.eventually.be.ok.then(function(value) {
            logg('Signeringsknapp är klickbar' + value);
        }, function(reason) {
            callback('FEL, Signeringsknapp är inte klickbar,' + reason);
        }).then(callback);
    });
};
