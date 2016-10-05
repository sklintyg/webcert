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
/*globals pages, wcTestTools*/

'use strict';

var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var testdataHelper = wcTestTools.helpers.testdata;
var helpers = require('./helpers.js');

var date;
var key;
var digit;

var pattern = /\d{4}\-\d{2}\-\d{2}/g;

function storeDate(tmpDate) {
    date = tmpDate;
}

function storeDigit(tmpDigit) {
    digit = tmpDigit;
}

function storeKey(tmpKey) {
    key = tmpKey;
}

module.exports = function() {

    this.Given(/^jag fyller i ett from datum$/, function(callback) {
        helpers.getRandomNedsattKey().then(function(nedsattKey) {
            storeKey(nedsattKey);
            fkUtkastPage.nedsatt[nedsattKey].from.sendKeys(testdataHelper.dateFormat(new Date()));
        }).then(callback);
    });


    this.Given(/^jag fyller i kortkommando som till och med datum$/, function(callback) {
        helpers.getRandomDigit(999).then(function(randomDigit) {
            storeDigit(randomDigit);

            fkUtkastPage.nedsatt[key].tom.sendKeys('d' + randomDigit).then(function() {
                fkUtkastPage.baserasPa.minUndersokning.datum.sendKeys(testdataHelper.dateFormat(new Date()));
                fkUtkastPage.nedsatt[key].tom.getAttribute('value').then(function(date) {
                    storeDate(date);
                }).then(callback);
            });
        });
    });

    this.Given(/^ska till och med datum räknas ut automatiskt$/, function() {
        var result = pattern.test(date);

        if (result) {
            var futureDate = new Date();
            futureDate.setDate(futureDate.getDate() + digit);
            var formattedFutureDate = testdataHelper.dateFormat(futureDate);
            expect(date).to.equal(formattedFutureDate);
        }
    });
};
