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

/**
 * Created by bennysce on 17/12/15.
 */
/* globals browser, protractor */
'use strict';

module.exports = {
    sekretessmarkering: element(by.id('sekretessmarkering')),
    namnFortsatt: element(by.id('namnFortsatt')),

    get: function() {
        browser.get('#/create/choose-intyg-type/default/index');
    },
    isAt: function() {
        return element(by.id('sokSkrivValjUtkastType')).isDisplayed();
    },
    createUtkast: function(IntygTypShortcode) {
        return element(by.id('intygTypeFortsatt-' + IntygTypShortcode)).sendKeys(protractor.Key.SPACE);
    },
    clickFornyaBtnById: function(id) {
        return element(by.id('fornyaBtn-' + id)).click();
    },
};
