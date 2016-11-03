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
/* globals pages,logger */

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
};
