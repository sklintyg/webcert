/* globals pages, browser, intyg, it, describe */

'use strict';

module.exports = function () {

    this.Given(/^fyller i alla nödvändiga fält för intyget$/, function (callback) {
        global.pages.intygpages.fkUtkast.smittskyddCheckboxClick();
        global.pages.intygpages.fkUtkast.nedsattMed25CheckboxClick();
        callback();
    });

    this.Given(/^signerar "Läkarintyg FK 7263"-intyget$/, function (callback) {

        global.pages.intygpages.fkUtkast.whenSigneraButtonIsEnabled().then(function () {
            global.pages.intygpages.fkUtkast.signeraButtonClick();
        });

        //Fånga intygets id
        if (!global.intyg) {
            global.intyg = {};

        }
        global.browser.getCurrentUrl().then(function (text) {
            global.intyg.id = text.split('/').slice(-1)[0];
            global.intyg.id = global.intyg.id.replace('?signed', '');
        });

        callback();
    });

    this.Then(/^ska "Läkarintyg FK 7263"-intygets status vara "([^"]*)"$/, function (statustext, callback) {
        expect(element(by.id("certificate-is-sent-to-it-message-text")).getText()).to.eventually.contain(statustext).and.notify(callback);
    });
    
    this.Given(/^ska intyget finnas i Mina intyg$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions

        var certBox = element(by.id('certificate-#' + global.intyg.id));

        console.log('Verifierar att intyget finns i ... ' + certBox);
        
        expect(certBox.element(by.cssContainingText('.ng-binding', 'Inkom till Mina intyg'))
               .isPresent()).to.eventually.to.equal(true).and.notify(callback);
        
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
