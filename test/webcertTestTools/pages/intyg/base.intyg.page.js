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

/**
 * Created by bennysce on 02-12-15.
 */
/*globals browser, protractor, Promise, logger*/
'use strict';

var JClass = require('jclass');
var testTools = require('common-testtools');
var testdataHelper = testTools.testdataHelper;
testTools.protractorHelpers.init();
var shuffle = testdataHelper.shuffle;
var restUtil = require('../../util/rest.util.js');
var pageHelpers = require('../pageHelper.util.js');
var hogerfaltet = require('./hogerfaltet');


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

            statusRevoked: element(by.css('#intygstatus1 [data-intyg-status-code="is-004"]'))
        };
        this.intygHeader = element(by.id('intyg-vy-laddad'));
        this.intygStatus = [
            element(by.id('intygstatus1')),
            element(by.id('intygstatus2'))
        ];
        this.patientNamnOchPersonnummer = element(by.id('patientNamnPersonnummer'));
        this.FdPersonnummer = element(by.css('.old-person-id'));
        this.skicka = {
            knapp: element(by.id('sendBtn')),
            dialogKnapp: element(by.id('button1send-dialog')),
            statusSent: element(by.css('#intygstatus1 [data-intyg-status-code="is-002"]'))
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
        this.arendeFilterKompletteringsbegaran = element(by.id('arende-filter-kompletteringsbegaran'));
        this.arendeFilterAdministrativafragor = element(by.id('arende-filter-administrativafragor'));
        this.arendePanel = element(by.css('arende-panel'));
        this.arendeText = element(by.id('arendeNewModelText'));
        this.arendeAmne = element(by.id('new-question-topic'));
        this.arendeAmneSelected = element(by.id('new-question-topic-selected-item-label'));
        this.arendeSend = element(by.id('sendArendeBtn'));

        /* Komplettera modaler */
        this.kompletteraIntygButton = element(by.id('komplettera-intyg'));
        this.kanInteKompletteraButton = element(by.id('kan-inte-komplettera'));
        this.kanInteKompletteraModalAnledning1 = element(by.id('komplettering-modal-dialog-anledning-1'));
        this.kanInteKompletteraModalOvrigaUpplysningar = element(by.id('komplettering-modal-dialog-ovriga-upplysningar'));
        this.kanInteKompletteraModalAnledning2 = element(by.id('komplettering-modal-dialog-anledning-2'));
        this.kanInteKompletteraModalMeddelandeText = element(by.id('komplettering-modal-dialog-meddelandetext'));
        this.kanInteKompletteraModalSkickaSvarButton = element(by.id('komplettering-modal-dialog-send-answer-button'));
        this.kompletteringBesvaradesMedMeddelandeAlert = element(by.id('arende-komplettering-besvarades-med-meddelande-alert'));
        this.kompletteringUtkastLink = element(by.id('komplettera-open-utkast'));
        this.uthoppKompletteraLink = element(by.id('arende-komplettering-uthopp-link'));

        /* Fråga/svar element i högerfältet */
        this.fragaSvar = hogerfaltet.fragaSvar;

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

        this.skrivUtBtn = element(by.id('downloadprint'));
        this.selectUtskriftButton = element(by.id('intyg-header-dropdown-select-pdf-type'));

        this.newPersonIdMessage = element(by.id('wc-new-person-id-message'));
        this.newPersonIdMessageText = element(by.id('wc-new-person-id-message-text'));
    },
    get: function(intygId) {
        browser.get('/#/intyg/' + this.intygType + '/' + intygId + '/');
    },
    scrollIntoView: function(domId) {
        browser.executeScript('if ($("#' + domId + '").length) { $("#' + domId + '")[0].scrollIntoView()}');
        return domId;
    },
    getReason: function(reasonBth) {
        for (var key in this.makulera) {
            if (reasonBth === key) {
                return Promise.resolve(this.makulera[key]);
            }
        }
    },
    pickMakuleraOrsak: function(optionalOrsak) {
        let mojligaOrsaker = {
            felPatient: {
                labelTxt: 'Intyget har utfärdats på fel patient.',
                radio: this.makulera.dialogRadioFelPatient,
                txt: this.makulera.dialogRadioFelPatientClarification
            },
            annatAlvarligtFel: {
                labelTxt: 'Annat allvarligt fel.',
                radio: this.makulera.dialogRadioAnnatAllvarligtFel,
                txt: this.makulera.dialogRadioAnnatAllvarligtFelClarification
            }
        };

        let radioElm;
        let txtElm;
        let orsak = (optionalOrsak) ? optionalOrsak : shuffle(['fel patient', 'allvarligt fel'])[0];

        if (orsak && mojligaOrsaker.felPatient.labelTxt.indexOf(orsak) !== -1) {
            radioElm = mojligaOrsaker.felPatient.radio;
            txtElm = mojligaOrsaker.felPatient.txt;
        } else if (orsak && mojligaOrsaker.annatAlvarligtFel.labelTxt.indexOf(orsak) !== -1) {
            radioElm = mojligaOrsaker.felPatient.radio;
            txtElm = mojligaOrsaker.felPatient.txt;
        }
        logger.debug('Väljer orsak: ' + orsak);

        return pageHelpers.moveAndSendKeys(radioElm, protractor.Key.SPACE)
            .then(function() {
                return browser.sleep(1500);
            })
            .then(function() {
                return pageHelpers.moveAndSendKeys(txtElm, 'Beskrivning för ' + orsak);
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
        var at = this.at;
        return browser.wait(function() {
            return at.isPresent();
        }, 5000);
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
        this.arendeFilterAdministrativafragor.click();
        pageHelpers.moveAndSendKeys(this.arendeText, arendeText);
        this.arendeValjAmne(arendeAmne);
        this.arendeSend.click();

        return this.arendePanel.all(by.css('arende-panel > div')).first().getAttribute('id').then(function(id) {
            return Promise.resolve(id.substring('arende-administrativaFragor-'.length));
        });
    },
    arendeValjAmne: function(val) {
        this.arendeAmne.click();
        this.arendeAmne.all(by.css('.plate div')).getByText(val).then(function(elm) {
            elm.click();
        });
    },
    hasState: function(states, state) {
        for (var a = 0; a < states.length; a++) {
            if (states[a].state === state) {
                return true;
            }
        }
        return false;
    },
    getArendeById: function(komplettering, id) {
        var subgroup = 'administrativaFragor';
        if (komplettering) {
            subgroup = 'kompletteringar';
        }
        return element(by.id('arende-' + subgroup + '-' + id));
    },
    getArendeAdministrativaFragorAmneById: function(id) {
        return element(by.css('#arende-panel-header-amne-' + id)).getText();
    },
    getArendeAdministrativaFragorTextById: function(id) {
        return element(by.css('#administrativaFragor-arende-fragetext-' + id)).getText();
    },
    getArendeAdministrativaSvarTextById: function(id) {
        return element(by.css('#administrativaFragor-arende-svartext-' + id)).getText();
    },
    getArendeHandledCheckbox: function(id) {
        return element(by.css('#handleCheck-' + id));
    },
    getIntygHasKompletteringMessage: function() {
        return element(by.css('#intygstatus1 [data-intyg-status-code="is-006"]'));
    },
    getKompletteringSvarTextById: function(id) {
        return element(by.css('#kompletteringar-arende-svartext-' + id)).getText();
    },
    getIntygKompletteringFrageText: function(frageId, index) {
        return element(by.id('inline-komplettering-' + frageId + '-' + index));
    },
    getAnswerButton: function(id) {
        return element(by.id(this.scrollIntoView('arende-answer-button-' + id)));
    },
    getAnswerBox: function(id) {
        return element(by.id('answerText-' + id));
    },
    getSendAnswerButton: function(id) {
        return element(by.id(this.scrollIntoView('sendAnswerBtn-' + id)));
    },
    getKompletteringDisabledSign: function(id) {
        return element(by.id('komplettering-disabled-' + id));
    },
    markArendeAsHandled: function(id) {
        return element(by.id('handleCheck-' + id));
    },
    waitUntilIntygInIT: function(intygsId) {
        browser.wait(function() {
            var innerDefer = protractor.promise.defer();
            restUtil.getIntyg(intygsId).then(function(intygBody) {
                if (intygBody.body) {
                    innerDefer.fulfill(true);
                } else {
                    innerDefer.fulfill(false);
                }
            }, function(error) {
                innerDefer.reject(error);
            });
            return innerDefer.promise;
        }, 10000);
    },
    openReceiverApprovalDialog: function() {
        return element(by.id('open-approve-receivers-dialog-btn')).click();
    },
    getReceiverApprovalDialog: function() {
        return element(by.id('wc-approvereceivers-dialog'));
    },
    clickReceiverApprovalOption: function(receiverId, value) {
        return element(by.id('approve-receiver-' + receiverId + '-radio-' + value)).click();
    },
    closeReceiverApproval: function(save) {
        if (save) {
            return element(by.id('save-approval-settings-btn')).click();
        } else {
            return element(by.id('cancel-approval-settings-btn')).click();
        }

    }
});


module.exports = BaseIntyg;
