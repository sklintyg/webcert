/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var viewCertAndQa = element(by.id('viewCertAndQA')),
    copyBtn = element(by.id('copyBtn')),
    dialogCopyBtn = element(by.id('button1copy-dialog'));

module.exports = {
    get: function(intygId) {
        browser.get('/web/dashboard#/intyg/ts-bas/' + intygId);
    },
    viewCertAndQaIsDisplayed: function(){
        return viewCertAndQa.isDisplayed();
    },
    copy: function(){
        copyBtn.click();
    },
    copyDialogConfirm: function() {
        dialogCopyBtn.click();
    }
};
