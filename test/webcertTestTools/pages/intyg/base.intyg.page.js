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
/*globals browser, protractor*/
'use strict';

var JClass = require('jclass');

var BaseIntyg = JClass._extend({
    init: function() {
        this.intygType = null;
        this.at = element(by.id('viewCertAndQA'));
        this.makulera = {
            btn: element(by.id('makuleraBtn')),
            dialogAterta: element(by.id('button1makulera-dialog')),
            kvittensOKBtn: element(by.id('confirmationOkButton')),
            statusRevokeInprogress: element(by.id('certificate-revoked-it-message-text')),
            statusRevoked: element(by.id('certificate-is-revoked-message-text'))
        };
        this.skicka = {
            knapp: element(by.id('sendBtn')),
            samtyckeCheckbox: element(by.id('patientSamtycke')),
            dialogKnapp: element(by.id('button1send-dialog')),
            statusSendInprogress: element(by.id('certificate-is-on-sendqueue-to-it-message-text')),
            statusSent: element(by.id('certificate-is-sent-to-recipient-message-text'))
        };
        this.copy = {
            button: element(by.id('copyBtn')),
            dialogConfirmButton: element(by.id('button1copy-dialog'))
        };
        this.fornya = {
            button: element(by.id('fornyaBtn')),
            dialogConfirmButton: element(by.id('button1fornya-dialog'))
        };
        this.copyBtn = element(by.css('.btn.btn-info'));
        this.backBtn = element(by.id('tillbakaButton'));

        // Ärende
        this.signedMessage = element(by.id('intyg-is-sent-to-it-message-text'));

        this.arendeIntygNotSentYetMessage = element(by.id('intyg-is-not-sent-to-fk-message-text'));
        this.arendeSentMessage = element(by.id('arende-is-sent-to-fk-message-text'));

        this.newArendeBtn = element(by.id('askArendeBtn'));

        this.arendeText = element(by.id('arendeNewModelText'));
        this.arendeAmne = element(by.id('new-question-topic'));
        this.arendeSend = element(by.id('sendArendeBtn'));
    },
    get: function(intygId) {
        browser.get('/web/dashboard#/intyg/' + this.intygType + '/' + intygId);
    },
    isAt: function() {
        return this.at.isDisplayed();
    },
    send: function() {
        var self = this;
        return this.skicka.knapp.click().then(function() {
            return self.skicka.samtyckeCheckbox.click().then(function() {
                return self.skicka.dialogKnapp.click();
            });
        });
    },
    copyBtn: function() {
        return this.copy.button;
    },
    copyDialogConfirmBtn: function() {
        return this.copy.dialogConfirmButton;
    },
    fornyaDialogConfirmBtn: function() {
        return this.fornya.dialogConfirmButton;
    },
    skrivUtFullstandigtIntyg: function() {
        return element(by.cssContainingText('button', 'Skriv ut')).sendKeys(protractor.Key.SPACE).then(function() {
            return element(by.cssContainingText('a', 'Fullständigt intyg')).click();
        });
    },
    sendNewArende: function(arendeText, arendeAmne) {
        var self = this;
        return this.newArendeBtn.click().then(function(){
            return self.arendeText.sendKeys(arendeText).then(function() {
                return self.arendeValjAmne(arendeAmne).then(function() {
                    return self.arendeSend.click();
                });
            });
        });
    },
    arendeValjAmne: function(val) {
        return this.arendeAmne.all(by.css('option[label="' + val + '"]')).click();
    },
    hasState: function(states, state) {
        for(var a = 0; a < states.length; a++) {
            if (states[a].state === state) {
                return true;
            }
        }
        return false;
    }

});

module.exports = BaseIntyg;
