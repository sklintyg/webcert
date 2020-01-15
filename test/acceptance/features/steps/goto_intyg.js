/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
/*global browser, logger, protractor, JSON, wcTestTools */
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

var createIntygWithStatus = require('./helpers_create_intyg.js').createIntygWithStatus;
var helpers = require('./helpers.js');
var getIntygElementRow = helpers.getIntygElementRow;
var shuffle = wcTestTools.helpers.testdata.shuffle;

/*
 *	Stödfunktioner
 *
 */

function gotoIntyg(world, status, intygRadElement, cb) {
  //Om det inte finns några intyg att använda
  if (!intygRadElement) {
    logger.info('Hittade inget intyg, skapar ett nytt..');
    createIntygWithStatus(world, status).then(function() {

      //Gå till det nyskapade intyget
      helpers.getUrl(helpers.intygURL(world.intyg)).then(function() {
        cb();
      });
    });
  }
  //Gå in på intyg
  else {
    intygRadElement.element(by.cssContainingText('button', 'Visa')).sendKeys(protractor.Key.SPACE);
    cb();
  }
}

function getIER(world, status, callback) {
  let intyg = world.intyg;
  getIntygElementRow(intyg.typ, status, function(el) {
    gotoIntyg(world, status, el, function(err) {
      browser.getCurrentUrl().then(function(text) {
        intyg.id = text.split('/').slice(-2)[0];
        intyg.id = intyg.id.split('?')[0];
        logger.info('intyg.id:' + intyg.id);
        logger.info('Status: ' + status);
        if (err) {
          callback(JSON.stringify(err));
        } else {
          callback();
        }
      });
    });

  });
}

/*
 *	Test steg
 *
 */

When(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, {
  timeout: 700 * 1000
}, function(intygstyp, status, callback) {
  this.intyg.typ = intygstyp;
  getIER(this, status, callback);
});

When(/^jag går in på ett slumpat SMI\-intyg med status "([^"]*)"$/, {
  timeout: 700 * 1000
}, function(status, callback) {
  var randomIntyg = shuffle([
    'Läkarintyg för sjukpenning',
    'Läkarutlåtande för sjukersättning',
    'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
    'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång'
  ])[0];
  logger.info('Intyg type: ' + randomIntyg);
  this.intyg.typ = randomIntyg;
  getIER(this, status, callback);
});

When(/^jag går in på ett slumpat intyg med status "([^"]*)"$/, {
  timeout: 700 * 1000
}, function(status, callback) {
  var randomIntyg = shuffle([
    'Läkarutlåtande för sjukersättning',
    'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga',
    'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång',
    //'Läkarintyg FK 7263', //Disabled i fristående läge och ersätts av Lisjp.
    'Transportstyrelsens läkarintyg högre körkortsbehörighet',
    'Transportstyrelsens läkarintyg diabetes',
    'Arbetsförmedlingens medicinska utlåtande'
  ])[0];
  logger.info('Intyg type: ' + randomIntyg);
  this.intyg.typ = randomIntyg;
  getIER(this, status, callback);
});

When(/^jag går in på ett slumpat TS\-intyg med status "([^"]*)"$/, {
  timeout: 700 * 1000
}, function(status, callback) {
  var randomIntyg = shuffle([
    'Transportstyrelsens läkarintyg högre körkortsbehörighet',
    'Transportstyrelsens läkarintyg diabetes'
  ])[0];
  logger.info('Intyg type: ' + randomIntyg);
  this.intyg.typ = randomIntyg;
  getIER(this, status, callback);
});

When(/^jag går in på ett slumpat AF\-intyg med status "([^"]*)"$/, {
  timeout: 700 * 1000
}, function(status, callback) {
  var randomIntyg = shuffle([
    'Arbetsförmedlingens medicinska utlåtande'
  ])[0];
  logger.info('Intyg type: ' + randomIntyg);
  this.intyg.typ = randomIntyg;
  getIER(this, status, callback);
});
