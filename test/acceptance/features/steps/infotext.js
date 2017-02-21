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
/* globals logger , Promise*/

'use strict';

// var luseUtkastPage = pages.intyg.luse.utkast;

module.exports = function() {

    this.Given(/^ska ett info\-meddelande visa "([^"]*)"$/, function(text) {
        var alerts = element.all(by.css('.alert-info')).map(function(elm, index) {
            return elm.getText();
        });

        return alerts.then(function(alertTexts) {
            var joinedTexts = alertTexts.join('\n');
            logger.info('Hittade info-meddelanden: ' + joinedTexts);
            return expect(joinedTexts).to.include(text);
        });


    });

    this.Given(/^ska ett varning\-meddelande visa "([^"]*)"$/, function(text) {
        var alerts = element.all(by.css('.alert-warning')).map(function(elm, index) {
            return elm.getText();
        });

        return alerts.then(function(alertTexts) {
            var joinedTexts = alertTexts.join('\n');
            logger.info('Hittade varning-meddelanden: ' + joinedTexts);
            return expect(joinedTexts).to.include(text);
        });


    });

    this.Given(/^ska ett fel\-meddelande visa "([^"]*)"$/, function(text) {
        var alerts = element.all(by.css('.alert-danger')).map(function(elm, index) {
            return elm.getText();
        });

        return alerts.then(function(alertTexts) {
            var joinedTexts = alertTexts.join('\n');
            logger.info('Hittade fel-meddelanden: ' + joinedTexts);
            return expect(joinedTexts).to.include(text);
        });
    });

    this.Given(/^ska jag få en dialog med texten "([^"]*)"$/, function(text) {
        var alerts = element.all(by.css('.modal-content')).map(function(elm, index) {
            return elm.getText();
        });

        return alerts.then(function(alertTexts) {
            var joinedTexts = alertTexts.join('\n');
            logger.info('Hittade modalinnehåll: ' + joinedTexts);
            return expect(joinedTexts).to.include(text);
        });
    });


    this.Given(/^ska jag (se|inte se) en rubrik med texten "([^"]*)"$/, function(synlighet, text) {

        var header3s = element.all(by.css('h3, h1')).map(function(elm, index) {
            return elm.getText();
        });

        return header3s.then(function(headerTexts) {
            var joinedTexts = headerTexts.join('\n');
            logger.info('Hittade rubriker: ' + joinedTexts);
            if (synlighet === 'se') {
                return expect(joinedTexts).to.include(text);
            } else {
                return expect(joinedTexts).to.not.include(text);
            }

        });


    });

    this.Given(/^ska jag (se|inte se) en lista med vad som saknas$/, function(synlighet) {
        if (synlighet === 'se') {
            return expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(true);
        } else {
            return expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(false);
        }


    });

    this.Given(/^ska jag se en lista med endast det saknade "([^"]*)"$/, function(saknat) {
        var str = 'Utkastet saknar uppgifter i följande avsnitt\n' + saknat;
        var promiseArray = []; // Write code here that turns the phrase above into concrete actions  
        promiseArray.push(expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(true));
        promiseArray.push(expect(element(by.id('visa-vad-som-saknas-lista')).getText()).to.eventually.equal(str));
        return Promise.all(promiseArray);

    });
    this.Given(/^ska utkastets statusheader meddela "([^"]*)"$/, function(meddelande) {
        return expect(element(by.id('intyget-sparat-och-ej-komplett-meddelande')).getText()).to.eventually.contain(meddelande);
    });
    this.Given(/^ska inget valideringsfel visas$/, function() {
        return expect(element(by.css('.alert-danger')).isDisplayed()).to.eventually.equal(false);

    });





};
