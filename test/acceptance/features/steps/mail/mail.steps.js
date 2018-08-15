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

/*global browser, user, logger*/

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


var mail = require('./mail');

/*
 *	Test steg
 *
 */

Then(/^ska jag få ett mejl med ämnet "([^"]*)"$/, function(amne) {
    logger.silly('intygsid:' + this.intyg.id);
    var textToSearchFor = process.env.WEBCERT_URL + 'webcert/web/user/certificate/' + this.intyg.id + '/questions?enhet=' + user.enhetId;

    logger.silly(textToSearchFor);
    return browser.sleep(30000).then(function() {
        return mail.readRecentMails()
            .then(function(mailArr) {
                logger.silly(mailArr);
                return mailArr.join(',');
            })
            .should.eventually.contain(textToSearchFor);
    });

});
