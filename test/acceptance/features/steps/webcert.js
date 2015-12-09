/* global pages, browser, protractor */

'use strict';

var fkUtkastPage = pages.intygpages.fk7263Utkast;
var fkIntygPage = pages.intygpages.fkIntyg;

module.exports = function () {

    this.Given(/^fyller i alla nödvändiga fält för intyget$/, function (callback) {
        fkUtkastPage.smittskyddCheckboxClick();
        fkUtkastPage.nedsattMed25CheckboxClick();
        callback();
    });

    this.Given(/^signerar FK7263-intyget$/, function (callback) {

        fkUtkastPage.whenSigneraButtonIsEnabled().then(function () {
            fkUtkastPage.signeraButtonClick();
        });

        browser.getCurrentUrl().then(function (text) {
            global.intyg.id = text.split('/').slice(-1)[0];
            global.intyg.id = global.intyg.id.replace('?signed', '');
        });

        callback();
    });
    
    // this.Given(/^ska intyget finnas i Mina intyg$/, function (callback) {
    //     // När "Visa intyget"-knappen syns är vi nöjda här.

    //     // TODO / FIXME!

    //     var id = 'viewCertificateBtn-'+ global.intyg.id;
    //     browser.wait(EC.elementToBeClickable(id), 10000);
    //     callback();
    // });
    

    this.Given(/^jag öppnar intyget$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^intyget är signerat$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^intyget är skickat till försäkringskassan$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^jag makulerar intyget$/, function (callback) {
        fkIntygPage.makulera.btn.click();
        fkIntygPage.makulera.dialogAterta.click();
        fkIntygPage.makulera.kvittensOKBtn.click()
        .then(callback);
    });
    
    this.Given(/^ska intyget visa varningen "([^"]*)"$/, function (arg1, callback) {
        expect(element(by.id('certificate-is-revoked-message-text')).getText())
            .to.eventually.contain(arg1).and.notify(callback);
    });
    
};
