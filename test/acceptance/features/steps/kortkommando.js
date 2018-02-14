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
/*globals pages, wcTestTools,logger,browser,protractor*/

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


var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var testdataHelper = wcTestTools.helpers.testdata;



/*
 *	Stödfunktioner
 *
 */
var startDate = new Date(); //today
startDate.setDate(startDate.getDate() + Math.floor(Math.random() * 50));
var date;
var key;
var digit;

var pattern = /\d{4}\-\d{2}\-\d{2}/g;



/*
 *	Test steg
 *
 */



Given(/^jag fyller i ett from datum$/, function() {
    key = testdataHelper.shuffle(['med25', 'med50', 'med75', 'med100'])[0];
    var start = testdataHelper.dateFormat(startDate);
    logger.info('Startdatum: ' + start);
    return fkUtkastPage.nedsatt[key].from.sendKeys(start);
});


Given(/^jag fyller i kortkommando som till och med datum$/, function() {
    digit = Math.floor(Math.random() * 999);
    var shortcode = 'd' + digit;
    logger.info('Kortkommando:' + shortcode);

    return fkUtkastPage.nedsatt[key].tom.sendKeys(shortcode).then(function() {
        return fkUtkastPage.nedsatt[key].tom.sendKeys(protractor.Key.TAB).then(function() {
            return browser.sleep(1000).then(function() {
                return fkUtkastPage.nedsatt[key].tom.getAttribute('value').then(function(dateValue) {
                    date = dateValue;
                });
            });
        });
    });
});

Given(/^ska till och med datum räknas ut automatiskt$/, function() {
    console.log(date);
    var result = pattern.test(date);
    if (result) {
        var futureDate = new Date();
        futureDate.setDate(startDate.getDate() + (digit - 1)); // -1 eftersom from-dagen räknas med 
        var formattedFutureDate = testdataHelper.dateFormat(futureDate);
        logger.info('Jämför ' + date + ' med ' + formattedFutureDate + '..');
        expect(date).to.equal(formattedFutureDate);
    } else {
        throw 'Felaktigt datumformat, ' + date;
    }
});
