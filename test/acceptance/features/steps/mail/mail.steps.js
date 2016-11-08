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

/*global intyg, browser, user*/

'use strict';
var mail = require('./mail');

module.exports = function() {

    this.Given(/^ska jag få ett mejl med ämnet "([^"]*)"$/, function(amne) {
        console.log('intygsid:' + intyg.id);
        //var mailLink = 'https://webcert.ip30.nordicmedtest.sjunet.org/webcert/web/user/certificate/' + intyg.id + '/questions?enhet';
        var textToSearchFor = 'https://webcert.ip30.nordicmedtest.sjunet.org/webcert/web/user/certificate/' + intyg.id + '/questions?enhet=' + user.enhetId;
        //user.enhetId;
        //var mailLink = 'https://webcert.ip30.nordicmedtest.sjunet.org/webcert/web/user/certificate/d5d28ab6-9907-d12b-cc93-52b5400e70c5/questions?enhet=TSTNMT2321000156-105F';
        //svar mailLink = 'nmt_vg1_Rehab har fått en fråga från Försäkringskassan angående ett intyg.';
        console.log(textToSearchFor);
        return browser.sleep(30000).then(function() {
            return mail.readRecentMails()
                .then(function(mailArr) {
                    console.log(mailArr);
                    return mailArr.join(',');
                })
                .should.eventually.contain(textToSearchFor);
        });

    });
};
