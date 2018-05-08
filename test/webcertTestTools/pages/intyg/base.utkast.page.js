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
 * Created by bennysce on 02-12-15.
 */
/*globals browser,protractor, Promise, logger*/
'use strict';

var pageHelpers = require('../pageHelper.util.js');
var JClass = require('jclass');
var EC = protractor.ExpectedConditions;
var hogerfaltet = require('./hogerfaltet');

var BaseUtkast = JClass._extend({
    init: function() {

        this.at = null;
        this.signeraButton = element(by.id('signera-utkast-button'));
        this.radera = {
            knapp: element(by.id('ta-bort-utkast')),
            bekrafta: element(by.id('confirm-draft-delete-button'))
        };
        this.skrivUtBtn = element(by.id('skriv-ut-utkast'));
        this.signingDoctorName = element(by.id('signingDoctor'));
        this.arendeQuestion = {
            newArendeButton: element(by.id('askArendeBtn')),
            text: element(by.id('arendeNewModelText')),
            topic: element(by.id('new-question-topic')),
            kontakt: element(by.cssContainingText('option', 'Kontakt')),
            sendButton: element(by.id('sendArendeBtn'))
        };
        this.fragaSvar = hogerfaltet.fragaSvar;
        this.newTextVersionAlert = element(by.id('newTextVersion'));
        this.backBtn = element(by.id('tillbakaButton'));
        this.showMissingInfoList = element(by.id('visa-vad-som-saknas-lista'));
        this.patientNamnPersonnummer = element(by.id('patientNamnPersonnummer'));
        this.patientNamnPersonnummerFd = element(by.css('.old-person-id'));
        /*this.sparatOchKomplettMeddelande = element(by.id('intyget-sparat-och-komplett-meddelande')); Finns inte i 6.0? */
        this.klartAttSigneraStatus = element(by.id('intygstatus1'));
        this.utkastStatus = element(by.id('intygstatus1'));
        this.sparatStatus = element(by.id('intygstatus2'));

        this.enhetensAdress = {
            postAdress: element(by.id('grundData-skapadAv-vardenhet-postadress')),
            postNummer: element(by.id('grundData-skapadAv-vardenhet-postnummer')),
            postOrt: element(by.id('grundData-skapadAv-vardenhet-postort')),
            enhetsTelefon: element(by.id('grundData-skapadAv-vardenhet-telefonnummer'))
        };
        this.patientAdress = {
            postAdress: element(by.id('grundData-patient-postadress')),
            postNummer: element(by.id('grundData-patient-postnummer')),
            postOrt: element(by.id('grundData-patient-postort'))
        };

        this.newPersonIdMessage = element(by.id('wc-new-person-id-message'));
        this.newPersonIdMessageText = element(by.id('wc-new-person-id-message-text'));
    },
    get: function(intygType, intygId) {
        browser.get('/#/' + intygType + '/edit/' + intygId + '/');
    },
    isAt: function() {
        var at = this.at;
        return browser.wait(function() {
            return at.isPresent();
        }, 5000);
    },
    isSigneraButtonEnabled: function() {
        return this.signeraButton.isEnabled();
    },
    whenSigneraButtonIsEnabled: function() {
        browser.wait(EC.elementToBeClickable(this.signeraButton), 10000);
    },
    signeraButtonClick: function() {
        return pageHelpers.moveAndSendKeys(this.signeraButton, protractor.Key.SPACE);
    },
    getMissingInfoMessagesCount: function() {
        return this.showMissingInfoList.all(by.tagName('a')).then(function(items) {
            return items.length;
        });
    },
    enableAutosave: function() {
        browser.executeScript(function() {
            window.autoSave = true;
        });
    },
    disableAutosave: function() {
        browser.executeScript(function() {
            window.autoSave = false;
        });
    },
    angeEnhetAdress: function(adressObj) {

        var postAddrEL = this.enhetensAdress.postAdress;
        var postOrtEL = this.enhetensAdress.postOrt;
        var enhetTelefonEL = this.enhetensAdress.enhetsTelefon;
        var postNummerEL = this.enhetensAdress.postNummer;


        return postAddrEL.clear().then(function() {
            return pageHelpers.moveAndSendKeys(postAddrEL, adressObj.postadress);
        }).then(function() {
            return postNummerEL.clear();
        }).then(function() {
            return pageHelpers.moveAndSendKeys(postNummerEL, adressObj.postnummer);
        }).then(function() {
            return postOrtEL.clear();
        }).then(function() {
            return pageHelpers.moveAndSendKeys(postOrtEL, adressObj.postort);
        }).then(function() {
            enhetTelefonEL.clear();
        }).then(function() {
            return pageHelpers.moveAndSendKeys(enhetTelefonEL, adressObj.telefon);
        });
    },

    angePatientAdress: function(adressObj) {
        var postAddrEL = this.patientAdress.postAdress;
        var postNummerEL = this.patientAdress.postNummer;
        var postOrtEL = this.patientAdress.postOrt;
        return Promise.all([postAddrEL.isEnabled(), postNummerEL.isEnabled(), postOrtEL.isEnabled()]).then(values => {
                if (values[0] && values[1] && values[2]) {
                    return Promise.resolve();
                } else {
                    return Promise.reject('Kan inte fylla i adress - adressfält inaktiverade');
                }
            }).then(() => postAddrEL.clear())
            .then(() => pageHelpers.moveAndSendKeys(postAddrEL, adressObj.postadress))
            .then(() => postNummerEL.clear())
            .then(() => pageHelpers.moveAndSendKeys(postNummerEL, adressObj.postnummer))
            .then(() => postOrtEL.clear())
            .then(() => pageHelpers.moveAndSendKeys(postOrtEL, adressObj.postort));
    },

    radioknappVal: function(val, text) {
        browser.ignoreSynchronization = true;
        logger.info(`Svarar ${val} i frågan ${text}`);
        return element.all(by.cssContainingText('.ue-fraga', text))
            .all(by.cssContainingText('.wc-radio', val))
            .all(by.tagName('input')).first().click()
            .then(() => browser.ignoreSynchronization = false);
    },

    checkboxVal: function(checkboxText) {
        logger.info(`Bockar i ${checkboxText}`);
        browser.ignoreSynchronization = true;
        return element.all(by.css('.ue-fraga'))
            .all(by.cssContainingText('wc-checkbox-wrapper', checkboxText))
            .all(by.tagName('input')).first().click()
            .then(() => browser.ignoreSynchronization = false);
    },

    dropdownVal: function(val, text) {
        browser.ignoreSynchronization = true;
        logger.info(`Väljer ${val} i dropdowner med text ${text}`);
        return element.all(by.cssContainingText('.ue-fraga', text))
            .all(by.tagName('wc-dropdown'))
            .each(el => el.click() // Klicka på dropdown
                .then(() => element.all(by.repeater('item in items')) // Ta fram alternativen
                    .filter(el => el.getText().then(t => t === val)) // Välj den som har samma text som argumentet
                    .click()))
            .then(() => browser.ignoreSynchronization = false);
    },

    fyllTextfalt: function(field, text) {
        browser.ignoreSynchronization = true;
        logger.info(`Fyller i ${text} i textfältet ${field}`);
        return element.all(by.cssContainingText('.ue-fraga', field))
            .all(by.css('input[type=text]'))
            .each(el => el.clear()
                .then(() => el.sendKeys(text)))
            .then(() => browser.ignoreSynchronization = false);
    }
});

module.exports = BaseUtkast;
