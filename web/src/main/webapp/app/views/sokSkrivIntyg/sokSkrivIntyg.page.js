/**
 * Created by stephenwhite on 09/06/15.
 */
var SokSkrivIntygPage = function() {
    'use strict';

    var doctor = element(by.css('.logged-in')),
        personnummer = element(by.id('pnr')),
        pnButton = element(by.id('skapapersonnummerfortsatt')),
        intygTypeSelector = element(by.id('intygType')),
        intygTypeButton = element(by.id('intygTypeFortsatt'));

    this.get = function() {
        browser.get('web/dashboard#/create/choose-cert-type/index');
    };

    this.getDoctorText = function() {
        return doctor.getText();
    };

    this.setPersonnummer = function(pn){
        personnummer.sendKeys(pn);
    };

    this.selectPersonnummer = function(pn){
        this.setPersonnummer(pn);
        pnButton.click();
    };

    this.selectIntygType = function(index){
        intygTypeSelector.all(by.css('option[value="' + index + '"]')).click();
    };

    this.continue = function(){
        intygTypeButton.click();
    };
};

module.exports = SokSkrivIntygPage;
