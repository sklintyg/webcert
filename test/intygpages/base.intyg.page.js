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
        browser.get('/web/dashboard#/' + this.intygType + '/edit/' + intygId);
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
