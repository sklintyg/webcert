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

/* global pages, intyg, browser, protractor */

'use strict';

var fkUtkastPage = pages.intyg.fk['7263'].utkast;
var basIntyg = pages.intyg.base.intyg;

module.exports = function() {

    this.Given(/^jag raderar utkastet$/, function(callback) {
        fkUtkastPage.radera.knapp.click();
        fkUtkastPage.radera.bekrafta.click()
            .then(callback);
    });

    this.Given(/^jag går tillbaka till start$/, function(callback) {
        browser.driver.wait(protractor.until.elementIsVisible(basIntyg.backBtn));
        basIntyg.backBtn.click().then(callback);
    });

    this.Given(/^ska intyget visa varningen "([^"]*)"$/, function(arg1, callback) {
        expect(element(by.id('certificate-is-revoked-message-text')).getText())
            .to.eventually.contain(arg1).and.notify(callback);
    });

    //   this.Given(/^ska intyget "([^"]*)" med status "([^"]*)" inte synas mer$/, function (intyg, status, callback) {
    //     var qaTable = element(by.css('table.table-qa'));

    //     qaTable.all(by.cssContainingText('tr', intyg)).filter(function(elem, index) {
    //         return elem.getText().then(function(text) {
    //             return (text.indexOf(status) > -1);
    //         });
    //     }).then(function(filteredElements) {
    //         expect(element(by.cssContainingText('button', 'Kopiera')).isPresent()).to.become(false).and.notify(callback);
    //         callback();
    //     });
    // });

    this.Given(/^ska intyget inte finnas i intygsöversikten$/, function(callback) {
        element(by.id('intygFilterSamtliga')).click();
        expect(element(by.id('showBtn-' + intyg.id)).isPresent()).to.become(false).and.notify(callback);
    });

};
