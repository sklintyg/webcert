/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var doctor = element(by.css('.logged-in')),
    personnummer = element(by.id('pnr')),
    pnButton = element(by.id('skapapersonnummerfortsatt')),
    intygTypeSelector = element(by.id('intygType')),
    intygTypeButton = element(by.id('intygTypeFortsatt')),
    sekretessmarkering = element(by.id('sekretessmarkering')),
    puerror = element(by.id('puerror')),
    fornamn = element(by.id('fornamn')),
    efternamn = element(by.id('efternamn')),
    namnFortsatt = element(by.id('namnFortsatt'));

module.exports = {
    get: function () {
        browser.get('web/dashboard#/create/choose-cert-type/index');
    },
    getDoctorText: function () {
        return doctor.getText();
    },
    setPersonnummer: function (pn) {
        personnummer.sendKeys(pn);
    },
    selectPersonnummer: function (pn) {
        this.setPersonnummer(pn);
        pnButton.click();
    },
    selectIntygType: function (index) {
        intygTypeSelector.all(by.css('option[value="' + index + '"]')).click();
    },
    selectIntygTypeByLabel: function (val) {
        intygTypeSelector.all(by.css('option[label="' + val + '"]')).click();
    },
    continueToUtkast: function () {
        intygTypeButton.click();
    },
    sekretessmarkering: sekretessmarkering,
    puerror: puerror,
    intygTypeSelector: intygTypeSelector,
    intygTypeButton: intygTypeButton,
    fornamn: fornamn,
    efternamn: efternamn,
    namnFortsatt: namnFortsatt
};
