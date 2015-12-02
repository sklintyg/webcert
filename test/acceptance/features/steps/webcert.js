/* globals pages, browser, intyg, it, describe */

'use strict';

module.exports = function () {

    this.Given(/^fyller i alla nödvändiga fält för intyget$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        // 
        console.log('Fyller i värden för intyget...');

        global.pages.intygpages.fkUtkast.smittskyddCheckboxClick();
        global.pages.intygpages.fkUtkast.nedsattMed25CheckboxClick();

        callback();
    });

    this.Given(/^signerar "Läkarintyg FK 7263"-intyget$/, {
        timeout: 100 * 2000
    }, function (callback) {

        global.pages.intygpages.fkUtkast.whenSigneraButtonIsEnabled().then(function () {
            global.pages.intygpages.fkUtkast.signeraButtonClick();
        });

        callback();
    });

    this.Then(/^ska "Läkarintyg FK 7263"-intygets status vara "([^"]*)"$/, {
        timeout: 100 * 2000
    }, function (statustext, callback) {
        expect(element(by.id("certificate-is-sent-to-it-message-text")).getText()).to.eventually.contain(statustext).and.notify(callback);
    });
    
    this.Given(/^jag går till mvk på patienten "([^"]*)"$/, function (arg1, callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
    });

    this.Given(/^ska intyget finnas i mvk$/, function (callback) {
        // Write code here that turns the phrase above into concrete actions
        callback.pending();
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
