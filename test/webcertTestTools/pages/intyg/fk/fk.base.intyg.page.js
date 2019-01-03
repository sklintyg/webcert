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

/*globals element, by, protractor */
'use strict';

/**
 * This is a base (view) page for fk SIT family of intyg (luse, lusi, luae_fs, luae_na).
 * Only things relevant to ALL such types should end up here.
 */

var BaseIntyg = require('../base.intyg.page.js');

var FkBaseIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygTypeVersion = null; //Overidden by subclasses

        this.at = element(by.id('viewCertAndQA'));
        this.enhetsAdress = {
            postAdress: element(by.id('vardperson_postadress')),
            postNummer: element(by.id('vardperson_postnummer')),
            postOrt: element(by.id('vardperson_postort')),
            enhetsTelefon: element(by.id('vardperson_telefonnummer'))
        };
    },
    //Locates the dynamic text based on text-key. see luaefs.dynamictexts.spec.js for example
    getDynamicLabelText: function(textKey) {
        return element(by.xpath('//span[@key="' + textKey + '"]')).getText();
    },
    getQAElementByText: function(containingText) {
        var panel = element(by.cssContainingText('.arende-panel', containingText));
        return {
            panel: panel,
            text: panel.element(by.css('textarea')),
            sendButton: panel.element(by.css('.btn-success'))
        };
    },
    clickKompletteraIntyg: function() {
        return element(by.id('komplettera-intyg')).sendKeys(protractor.Key.SPACE);
    },
    switchToArendeTab: function() {
        return element(by.id('tab-link-wc-arende-panel-tab')).click();
    }
});

module.exports = FkBaseIntyg;
