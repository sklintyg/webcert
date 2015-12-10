/**
 * Created by bennysce on 09/06/15.
 */
/* globals browser */
'use strict';

var viewCertAndQa = element(by.id('viewCertAndQA')),
    copyBtn = element(by.id('copyBtn')),
    dialogCopyBtn = element(by.id('button1copy-dialog'));

module.exports = {
    makulera: {
        btn: element(by.id('makuleraBtn')),
        dialogAterta: element(by.id('button1makulera-dialog')),
        kvittensOKBtn: element(by.id('confirmationOkButton'))
    },
    skicka:{
        knapp: element(by.id('sendBtn')),
        samtyckeCheckbox:element(by.id('patientSamtycke')),
        dialogKnapp:element(by.id('button1send-dialog'))
    },
    radera: {
        knapp: element(by.id('ta-bort-utkast')),
        radera: element(by.id('confirm-draft-delete-button'))
    },
    get: function(intygId) {
        browser.get('/web/dashboard#/intyg/fk7263/' + intygId);
    },
    viewCertAndQaIsDisplayed: function() {
        return viewCertAndQa.isDisplayed();
    },
    copy: function() {
        copyBtn.click();
    },
    copyDialogConfirm: function() {
        dialogCopyBtn.click();
    }
};
