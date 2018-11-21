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

/* globals pages, logger, protractor, browser*/

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


var sokSkrivIntygPage = pages.sokSkrivIntyg.pickPatient;
var helpers = require('./helpers');

/*
 *	Stödfunktioner
 *
 */


/*
 *	Test steg
 *
 */


Given(/^ska jag( inte)? se en varning om kakor$/, function(inte, callback) {
    var shouldBeVisible = (inte === undefined); //Om 'inte' finns med i stegnamnet
    logger.silly('shouldBeVisible:' + shouldBeVisible);
    expect(sokSkrivIntygPage.cookie.consentBanner.element(by.tagName('button')).isPresent()).to.eventually.equal(shouldBeVisible).then(function() {
        if (!inte) {
            inte = '';
        }
        logger.info('OK - Varning syns' + inte);
    }, function(reason) {
        callback('FEL : ' + reason);
    }).then(callback);
});

Given(/^jag accepterar kakor$/, function(callback) {
    sokSkrivIntygPage.cookie.consentBtn.sendKeys(protractor.Key.SPACE)
        .then(callback());
});

Given(/^laddar om sidan$/, function() {
    return browser.refresh().then(function() {
        return helpers.mediumDelay();
    });
});
