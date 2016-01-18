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

/**
 * Created by bennysce on 02-12-15.
 */
/*globals browser*/
'use strict';

var Class = require('jclass');

var BaseUtkast = Class._extend({
    init: function() {
        this.at = null;
        this.signeraButton = element(by.id('signera-utkast-button'));
        this.pageHelper = null;
        this.radera = {
            knapp: element(by.id('ta-bort-utkast')),
            bekrafta: element(by.id('confirm-draft-delete-button'))
        };
    },
    get: function(intygType, intygId) {
        browser.get('/web/dashboard#/' + intygType + '/edit/' + intygId);
    },
    isAt: function() {
        return this.at.isDisplayed();
    },
    whenSigneraButtonIsEnabled: function() {
        return browser.wait(this.signeraButton.isEnabled());
    },
    signeraButtonClick: function() {
        this.signeraButton.click();
    }
});

module.exports = BaseUtkast;
