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
/*globals browser, protractor,Promise*/
'use strict';

var JClass = require('jclass');
var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;
var restUtil = require('../../util/rest.util.js');

var BaseIntyg = JClass._extend({
    init: function() {
        this.intygType = null;
        this.at = element(by.id('viewCertAndQA'));
        this.makulera = {
            btn: element(by.id('makuleraBtn')),
            dialogMakulera: element(by.id('button1makulera-dialog')),

            dialogRadioFelPatient: element(by.id('reason-FEL_PATIENT')),
            dialogRadioFelPatientClarification: element(by.id('clarification-FEL_PATIENT')),
            dialogRadioAnnatAllvarligtFel: element(by.id('reason-ANNAT_ALLVARLIGT_FEL')),
            dialogRadioAnnatAllvarligtFelClarification: element(by.id('clarification-ANNAT_ALLVARLIGT_FEL')),

            statusRevokeInprogress: element(by.id('certificate-is-revoked-message-text')),
            statusRevoked: element(by.id('certificate-revoked-it-message-text'))
        };
        this.patientNamnOchPersonnummer = element(by.id('patientNamnPersonnummer'));
        this.skicka = {
            knapp: element(by.id('sendBtn')),
            dialogKnapp: element(by.id('button1send-dialog')),
            statusSendInprogress: element(by.id('certificate-is-on-sendqueue-to-it-message-text')),
            statusSent: element(by.id('certificate-is-sent-to-recipient-message-text'))
        };
        this.fornya = {
            button: element(by.id('fornyaBtn')),
            dialogConfirmButton: element(by.id('button1fornya-dialog'))
        };
        this.replace = {
            button: element(by.id('ersattBtn')),
            dialogConfirmButton: element(by.id('button1ersatt-dialog')),
            dialogContinueButton: element(by.id('button2ersatt-dialog'))
        };
        this.backBtn = element(by.id('tillbakaButton'));

        // Ärende
        this.signedMessage = element(by.id('intyg-is-sent-to-it-message-text'));

        this.arendeIntygNotSentYetMessage = element(by.id('intyg-is-not-sent-to-fk-message-text'));
        this.arendeSentMessage = element(by.id('arende-is-sent-to-fk-message-text'));

        this.newArendeBtn = element(by.id('askArendeBtn'));

        this.arendeText = element(by.id('arendeNewModelText'));
        this.arendeAmne = element(by.id('new-question-topic'));
        this.arendeSend = element(by.id('sendArendeBtn'));

        // Statusmeddelanden vid namn/adressändring vid djupintegration
        this.statusNameChanged = element(by.id('intyg-djupintegration-name-changed'));
        this.statusAddressChanged = element(by.id('intyg-djupintegration-address-changed'));
        this.statusNameAndAddressChanged = element(by.id('intyg-djupintegration-name-and-address-changed'));

        this.patientAdress = {
            postadress: element(by.id('patient_postadress')),
            postnummer: element(by.id('patient_postnummer')),
            postort: element(by.id('patient_postort'))
        };

        this.enhetsAdress = {
            postAdress: element(by.id('vardperson_postadress')),
            postNummer: element(by.id('vardperson_postnummer')),
            postOrt: element(by.id('vardperson_postort')),
            enhetsTelefon: element(by.id('vardperson_telefonnummer'))
        };

        this.selectUtskriftButton = element(by.id('intyg-header-dropdown-select-pdf-type'));


    },
    get: function(intygId) {
        browser.get('/web/dashboard#/intyg/' + this.intygType + '/' + intygId + '/');
    },
    getReason: function(reasonBth) {
        for (var key in this.makulera) {
            if (reasonBth === key) {
                return Promise.resolve(this.makulera[key]);
            }
        }
    },
    pickMakuleraOrsak: function(optionalOrsak) {
        var makuleraDialog = element(by.cssContainingText('.modal-dialog', 'Makulera intyg'));
        var getMojligaOrsaker = makuleraDialog.all(by.css('label')).map(function(elm, index) {
            return elm.getText();
        });

        return getMojligaOrsaker.then(function(orsaker) {
            logger.debug(orsaker);
            var reason = shuffle(orsaker)[0];
            if (optionalOrsak) {
                reason = optionalOrsak;
            }
            logger.debug('Väljer orsak: ' + reason);
            return element(by.cssContainingText('label', reason)).sendKeys(protractor.Key.SPACE)
                .then(function() {
                    return browser.sleep(1500);
                })
                .then(function() {
                    return makuleraDialog.element(by.css('textarea')).sendKeys('Beskrivning för ') + reason;
                });
        });
    },

    getIntegration: function(intygId, params) {
        var url = '/visa/intyg/' + intygId;
        var first = true;
        Object.keys(params).forEach(function(name) {
            var value = params[name];
            if (first) {
                url += '?';
                first = false;
            } else {
                url += '&';
            }
            url += name + '=' + encodeURIComponent(value);
        });
        browser.get(url);
    },
    isAt: function() {
        return this.at.isDisplayed();
    },
    send: function() {
        var self = this;
        return this.skicka.knapp.click().then(function() {
            return self.skicka.dialogKnapp.click();
        });
    },
    replaceBtn: function() {
        return this.replace.button;
    },
    replaceDialogConfirmBtn: function() {
        return this.replace.dialogConfirmButton;
    },
    replaceDialogContinueBtn: function() {
        return this.replace.dialogContinueButton;
    },
    fornyaDialogConfirmBtn: function() {
        return this.fornya.dialogConfirmButton;
    },
    skrivUtFullstandigtIntyg: function() {
        return element(by.id('intyg-header-dropdown-select-pdf-type')).click().then(function() {
            return element(by.cssContainingText('a', 'Fullständigt intyg')).click();
        });
    },
    sendNewArende: function(arendeText, arendeAmne) {
        var self = this;
        return this.newArendeBtn.click().then(function() {
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
        for (var a = 0; a < states.length; a++) {
            if (states[a].state === state) {
                return true;
            }
        }
        return false;
    },
    getArendeById: function(handled, id) {
        var subgroup = 'unhandled';
        if (handled) {
            subgroup = 'handled';
        }
        return element(by.id('arende-' + subgroup + '-' + id));
    },
    getIntygHasKompletteringMessage: function() {
        return element(by.id('intyg-has-komplettering-message'));
    },
    getIntygKompletteringFrageText: function(frageId, index) {
        return element(by.id('inline-komplettering-' + frageId + '-' + index));
    },
    getAnswerBox: function(id) {
        return element(by.id('answerText-' + id));
    },
    getAnswerButton: function(id) {
        return element(by.id('sendAnswerBtn-' + id));
    },
    getKompletteringDisabledSign: function(id) {
        return element(by.id('komplettering-disabled-' + id));
    },
    markArendeAsHandled: function(id) {
        return element(by.id('handleCheck-' + id));
    },
    getOnlyLakareCanKompletteraSign: function(id) {
        return element(by.id('answerDisabledReasonPanel-' + id));
    },
    getKompletteraIntygButton: function(id) {
        return element(by.id('komplettera-intyg-' + id));
    },
    getUthoppKompletteraSvaraButton: function(id) {
        return element(by.id('uthopp-svara-med-meddelande-'+id))
    },
    getKompletteraIntygFortsattPaIntygsutkastButton: function(id) {
        return element(by.id('komplettera-open-utkast-' + id));
    },
    getKanInteKompletteraButton: function(id) {
        return element(by.id('kan-inte-komplettera-' + id));
    },
    getKompletteringsDialogLamnaOvrigaUpplysningar: function() {
        return element(by.id('komplettering-modal-dialog-answerWithNyttIntyg-button'));
    },
    getKompletteringsDialogSvaraMedMeddelandeButton: function() {
        return element(by.id('komplettering-modal-dialog-answerWithMessage-button'));
    },
    waitUntilIntygInIT: function(intygsId) {
        browser.wait(function() {
            var innerDefer = protractor.promise.defer();
            restUtil.getIntyg(intygsId).then(function(intygBody) {
                if (intygBody.body) {
                    innerDefer.fulfill(true);
                }
                else {
                    innerDefer.fulfill(false);
                }
            }, function(error) {
                innerDefer.reject(error);
            });
            return innerDefer.promise;
        }, 10000);
    }
});


module.exports = BaseIntyg;
