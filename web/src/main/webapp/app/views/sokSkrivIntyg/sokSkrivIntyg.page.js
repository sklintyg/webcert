/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var doctor = element(by.css('.logged-in')),
    personnummer = element(by.id('pnr')),
    pnButton = element(by.id('skapapersonnummerfortsatt')),
    intygTypeSelector = element(by.id('intygType')),
    intygTypeButton = element(by.id('intygTypeFortsatt'));

module.exports = {
    get: function() {
        browser.get('web/dashboard#/create/choose-cert-type/index');
    },
    getDoctorText: function() {
        return doctor.getText();
    },
    setPersonnummer: function(pn) {
        personnummer.sendKeys(pn);
    },
    selectPersonnummer: function(pn) {
        this.setPersonnummer(pn);
        pnButton.click();
    },
    selectIntygType: function(index) {
        intygTypeSelector.all(by.css('option[value="' + index + '"]')).click();
    },
    continueToUtkast: function() {
        intygTypeButton.click();
    }
};
