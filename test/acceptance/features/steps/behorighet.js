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

/* globals pages, protractor, logg */
'use strict';
var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var sokSkrivIntygUtkastTypePage = pages.sokSkrivIntyg.valjUtkastType;
// var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;

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

    this.Given(/^ska makuleraknappen inte vara tillgänglig$/, function(callback) {
        expect(fkIntygPage.makulera.btn.isPresent()).to.eventually.be.not.ok.then(function(value) {
            logg('Makuleraknappen syns inte (ok)' + value);
        }, function(reason) {
            callback('FEL, Makuleraknappen finns tillgänglig,' + reason);
        }).then(callback);
    });

	this.Given(/^väljer att byta vårdenhet$/, function (callback) {
	  element(by.id('wc-care-unit-clinic-selector-link')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^vårdenhet ska vara "([^"]*)"$/, function (arg1, callback) {
        expect(element(by.css('.clearfix')).getText()).to.eventually.contain(arg1).then(function(value) {
            logg('OK - vårdenhet = ' + value);
                }, function(reason) {
                    callback('FEL - vårdenhet: ' + reason);
                }).then(callback);
	});

	this.Given(/^jag väljer flik "([^"]*)"$/, function (arg1, callback) {
	  expect(element(by.id('menu-skrivintyg')).getText()).to.eventually.contain(arg1).then(function(value) {
		element(by.id('menu-skrivintyg')).click();
        logg('OK - byta flik till = ' + value);
                }, function(reason) {
                    callback('FEL - byta flik till: ' + reason);
                }).then(callback);
	});


	this.Given(/^jag väljer att byta vårdenhet$/, function (callback) {
		element(by.id('wc-care-unit-clinic-selector-link')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^väljer "([^"]*)"$/, function (arg1, callback) {
	   	element(by.id('wc-care-unit-clinic-selector-link')).click();
	   	element(by.id('select-active-unit-IFV1239877878-1046-modal')).sendKeys(protractor.Key.SPACE).then(callback);
	});

	this.Given(/^synns inte signera knappen$/, function (callback) {
		fkUtkastPage.signeraButton.isPresent().then(function (isVisible) {
		    if (isVisible) {
		        callback('FEL - Signera knapp synlig!');
		    } else {
				console.log('OK - Signera knapp ej synlig!');
		    }
		}).then(callback);
	});

	this.Given(/^synns Hämta personuppgifter knappen$/, function (callback) {
  		fkUtkastPage.fetchPatientButton.isPresent().then(function (isVisible) {
		    if (isVisible) {
				console.log('OK - Hämta personuppgifter synlig!');
		    } else {
		        callback('FEL - Hämta personuppgifter ej synlig!');
		    }
		}).then(callback);
	});

	this.Given(/^meddelas jag om spärren$/, function (callback) {
		expect(element(by.xpath('//*[@id=\"valj-intyg-typ\"]/div[1]/div/form/div[2]')).getText())
		.to.eventually.contain('På grund av sekretessmarkeringen går det inte att skriva nya elektroniska intyg.').then(function(value) {
			logg('OK - sekretessmarkeringe = ' + value);
                }, function(reason) {
                    callback('FEL - sekretessmarkeringe: ' + reason);
                }).then(callback);
	  
	});

	this.Given(/^jag kan inte gå in på att skapa ett "([^"]*)" intyg$/, function (arg1, callback) {
   		sokSkrivIntygUtkastTypePage.intygTypeButton.isDisplayed().then(function (isVisible) {
		    if (isVisible) {
		        callback('FEL - '+arg1+' synlig!');
		    } else {
				console.log('OK -'+arg1+' ej synlig!');
		    }
		}).then(callback);
	});

};
