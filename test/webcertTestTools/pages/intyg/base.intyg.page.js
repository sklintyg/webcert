/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

var BaseIntyg = Class._extend({
    init: function() {
        this.intygType = null;
        this.at = element(by.id('viewCertAndQA'));
        this.makulera = {
            btn: element(by.id('makuleraBtn')),
            dialogAterta: element(by.id('button1makulera-dialog')),
            kvittensOKBtn: element(by.id('confirmationOkButton'))
        };
        this.skicka = {
            knapp: element(by.id('sendBtn')),
            samtyckeCheckbox: element(by.id('patientSamtycke')),
            dialogKnapp: element(by.id('button1send-dialog'))
        };
        this.copy = {
            button: element(by.id('copyBtn')),
            dialogConfirmButton: element(by.id('button1copy-dialog'))
        };


    },
    get: function(intygId) {
        browser.get('/web/dashboard#/intyg/' + this.intygType + '/' + intygId);
    },
    isAt: function() {
        return this.at.isDisplayed();
    },
    copyBtn: function() {
        return this.copy.button;
    },
    copyDialogConfirmBtn: function() {
        return this.copy.dialogConfirmButton;
    }
});

module.exports = BaseIntyg;
