/* global pages, browser, protractor */

'use strict';

var EC = protractor.ExpectedConditions;

module.exports = function () {

    this.Given(/^fyller i alla nödvändiga fält för intyget$/, function (callback) {
        pages.intygpages.fkUtkast.smittskyddCheckboxClick();
        global.pages.intygpages.fkUtkast.nedsattMed25CheckboxClick();
        callback();
    });

    this.Given(/^signerar FK7263-intyget$/, function (callback) {

        global.pages.intygpages.fkUtkast.whenSigneraButtonIsEnabled().then(function () {
            global.pages.intygpages.fkUtkast.signeraButtonClick();
        });

        //Fånga intygets id
        if (!global.intyg) {
            global.intyg = {};

        }
        browser.getCurrentUrl().then(function (text) {
            global.intyg.id = text.split('/').slice(-1)[0];
            global.intyg.id = global.intyg.id.replace('?signed', '');
        });

        callback();
    });
    
    this.Given(/^ska intyget finnas i Mina intyg$/, function (callback) {
        // När "Visa intyget"-knappen syns är vi nöjda här.
        var id = 'viewCertificateBtn-'+ global.intyg.id;
        browser.wait(EC.elementToBeClickable(id), 10000);
        callback();
    });
    
    this.Given(/^att ett intyg är skapat$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^jag öppnar intyget$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^intyget är signerat$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^jag skickar intyget till Försäkringskassan$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^intyget är skickat till försäkringskassan$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^jag makulerar intyget$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^ska jag få en dialogruta som säger "([^"]*)"$/, function (arg1, callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^ska intyget visa varningen "([^"]*)"$/, function (arg1, callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^jag arkiverar intyget i mvk$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^ska intygets inte visas i mvk$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });
    
};
