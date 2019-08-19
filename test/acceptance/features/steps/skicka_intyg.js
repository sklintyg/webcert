/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/* globals pages */
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

var fkIntygPage = pages.intyg.fk['7263'].intyg;
var afUtkastPage = pages.intyg.af.af00213.utkast;
var helpers = require('./helpers');

/*
 *	Teststeg
 *
 */

Then(/^har intyget skickats till Arbetsförmedlingen$/, function(mottagare) {
  var utkastStatus = afUtkastPage.utkastStatus;
  return expect(utkastStatus.getText()).to.eventually.contain('Intyget är skickat till Arbetsförmedlingen');
});

When(/^jag skickar intyget till (.*)$/, function(mottagare) {
  if (!this.intyg.id) {
    browser.getCurrentUrl().then(function(text) {
      this.intyg.id = text.split('/').slice(-2)[0];
      this.intyg.id = this.intyg.id.split('?')[0];
      logger.info('Följande intygs id skickas till ' + mottagare + ': ' + this.intyg.id);
    });
  } else {
    logger.info('Följande intygs id skickas till ' + mottagare + ': ' + this.intyg.id);
  }

  return helpers.moveAndSendKeys(fkIntygPage.skicka.knapp, protractor.Key.SPACE).then(function() {
    return helpers.moveAndSendKeys(fkIntygPage.skicka.dialogKnapp, protractor.Key.SPACE);
  }).then(function() {
    //Vänta på att requesten processas av back-end.
    return helpers.pageReloadDelay();
  });

  // callback();
});
