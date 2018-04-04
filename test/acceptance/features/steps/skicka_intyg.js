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

/* globals pages */
/* globals browser, intyg, logger, protractor */

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
var helpers = require('./helpers');


/*
 *	Test steg
 *
 */

Given(/^jag skickar intyget till Transportstyrelsen/, function() {

    if (!intyg.id) {
        //Fånga intygets id
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            logger.info('Intygsid: ' + intyg.id);
            intyg.id = intyg.id.split('?')[0];
        });
    } else {
        logger.info('Följande intygs id skickas till Transportstyrelsen: ' + intyg.id);
    }
    return helpers.moveAndSendKeys(fkIntygPage.skicka.knapp, protractor.Key.SPACE).then(function() {
        helpers.moveAndSendKeys(fkIntygPage.skicka.dialogKnapp, protractor.Key.SPACE);
    });
});

Given(/^jag skickar intyget till Försäkringskassan$/, function() {


    if (!intyg.id) {
        browser.getCurrentUrl().then(function(text) {
            intyg.id = text.split('/').slice(-2)[0];
            intyg.id = intyg.id.split('?')[0];
            logger.info('Följande intygs id skickas till Försäkringskassan: ' + intyg.id);
        });
    } else {
        logger.info('Följande intygs id skickas till Försäkringskassan: ' + intyg.id);
    }



    return helpers.moveAndSendKeys(fkIntygPage.skicka.knapp, protractor.Key.SPACE).then(function() {
        return helpers.moveAndSendKeys(fkIntygPage.skicka.dialogKnapp, protractor.Key.SPACE);
    }).then(function() {
        //Vänta på att requesten processas av back-end.
        return helpers.mediumDelay();
    });

    // callback();
});
