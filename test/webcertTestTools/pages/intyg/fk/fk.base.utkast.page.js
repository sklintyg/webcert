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

/*globals element,by, protractor */
'use strict';

/**
 * This is a base page for fk SIT family of intyg (luse, lusi, luaefs, luaena).
 * Only things relevant to ALL such types should end up here.
 */

var BaseUtkast = require('../base.utkast.page.js');

var FkBaseUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));
        this.intygType = null;
        this.intygTypeVersion = null;

        // The "markera klart för att signera" functionality is common to all SIT / SMI intygstyper.
        this.markeraKlartForSigneringButton = element(by.id('markeraKlartForSigneringButton'));
        this.markeraKlartForSigneringModalYesButton = element(by.id('buttonYes'));
        this.markeradKlartForSigneringText = element(by.id('draft-marked-ready-text'));

        // Override, we do not handle patientAdress for FK-intyg.
        this.patientAdress = {

        };
    },
    //Locates the dynamic text based on text-key. see luaefs.dynamictexts.spec.js for example
    getDynamicLabelText: function(textKey) {
        return element(by.xpath('//span[@key="' + textKey + '"]')).getText();
    },
    //in ue-form-label the dynamic text is rendered differently
    getDynamicLabelTextById: function(id) {
        return element(by.id(id)).getText();
    },

    isMarkeraSomKlartAttSigneraButtonDisplayed: function() {
        return this.markeraKlartForSigneringButton.isDisplayed();
    },
    markeraSomKlartAttSigneraButtonClick: function() {
        this.markeraKlartForSigneringButton.sendKeys(protractor.Key.SPACE);
    }
});

module.exports = FkBaseUtkast;
