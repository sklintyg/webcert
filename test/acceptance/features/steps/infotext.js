/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

// var luseUtkastPage = pages.intyg.luse.utkast;

/*
 *	Stödfunktioner
 *
 */

/*
 *	Test steg
 *
 */

Then(/^ska ett info\-meddelande visa "([^"]*)"$/, function(text) {
  var alerts = element.all(by.css('.alert-info')).map(function(elm, index) {
    return elm.getText();
  });

  return alerts.then(function(alertTexts) {
    var joinedTexts = alertTexts.join('\n');
    logger.info('Hittade info-meddelanden: ' + joinedTexts);
    return expect(joinedTexts).to.include(text);
  });

});

Then(/^ska ett varning\-meddelande visa "([^"]*)"$/, function(text) {
  var alerts = element.all(by.css('.alert-warning')).map(function(elm, index) {
    return elm.getText();
  });

  return alerts.then(function(alertTexts) {
    var joinedTexts = alertTexts.join('\n');
    logger.info('Hittade varning-meddelanden: ' + joinedTexts);
    return expect(joinedTexts).to.include(text);
  });

});

Then(/^ska ett fel\-meddelande visa "([^"]*)"$/, function(text) {
  var alerts = element.all(by.css('.alert-danger')).map(function(elm, index) {
    return elm.getText();
  });

  return alerts.then(function(alertTexts) {
    var joinedTexts = alertTexts.join('\n');
    logger.info('Hittade fel-meddelanden: ' + joinedTexts);
    return expect(joinedTexts).to.include(text);
  });
});

Then(/^ska jag få en dialog med texten "([^"]*)"$/, function(text) {
  var alerts = element.all(by.css('.modal-content')).map(function(elm, index) {
    return elm.getText();
  });

  return alerts.then(function(alertTexts) {
    var joinedTexts = alertTexts.join('\n');
    logger.info('Hittade modalinnehåll: ' + joinedTexts);
    return expect(joinedTexts).to.include(text);
  });
});

Then(/^ska jag (se|inte se) en rubrik med texten "([^"]*)"$/, function(synlighet, text) {

  var headers = element.all(by.css('h3, h1')).map(function(elm, index) {
    return elm.getText();
  });

  return headers.then(function(headerTexts) {
    var joinedTexts = headerTexts.join('\n');
    logger.info('Hittade rubriker: ' + joinedTexts);
    if (synlighet === 'se') {
      return expect(joinedTexts).to.include(text);
    } else {
      return expect(joinedTexts).to.not.include(text);
    }

  });

});

Then(/^ska jag (se|inte se) en lista med vad som saknas$/, function(synlighet) {
  if (synlighet === 'se') {
    return expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(true);
  } else {
    return expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(false);
  }

});

Then(/^ska jag se en lista med endast det saknade "([^"]*)"$/, function(saknat) {
  var str = 'Utkastet saknar uppgifter i följande avsnitt\n' + saknat;
  var promiseArray = []; // Write code here that turns the phrase above into concrete actions  
  promiseArray.push(expect(element(by.id('visa-vad-som-saknas-lista')).isDisplayed()).to.eventually.equal(true));
  promiseArray.push(expect(element(by.id('visa-vad-som-saknas-lista')).getText()).to.eventually.equal(str));
  return Promise.all(promiseArray);

});
Then(/^ska utkastets statusheader meddela "([^"]*)"$/, function(meddelande) {
  return expect(element(by.id('intyget-sparat-och-ej-komplett-meddelande')).getText()).to.eventually.contain(meddelande);
});
Then(/^ska inget valideringsfel visas$/, function() {
  return expect(element(by.css('.alert-danger')).isDisplayed()).to.eventually.equal(false);

});
