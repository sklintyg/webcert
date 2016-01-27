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

	this.Given(/^jag clickar på Vidarebefodra$/, function (callback) {
	  //*[@id="unsignedCertTable"]/table/tbody/tr[5]/td[2]/button
	  //*[@id="unsignedCertTable"]/table/tbody/tr[6]/td[2]/button
	  element(by.xpath('//*[@id=\"unsignedCertTable\"]/table/tbody/tr[2]/td[2]/button')).sendKeys(protractor.Key.SPACE).then(callback);
	  // callback.pending();
	});

	this.Given(/^bekräftar vidarebefodran$/, function (callback) {
	  // Write code here that turns the phrase above into concrete actions
	  //*[@id="unsignedCertTable"]/table/tbody/tr[2]/td[2]/button
	  callback('TBI!');
	  // element(by.id('selected')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^ska intyget vara markerat som vidarebefodrad$/, function (callback) {
	  // Write code here that turns the phrase above into concrete actions
	  callback('TBI!');
	});
};
