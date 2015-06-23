/**
 * Created by stephenwhite on 09/06/15.
 */
var UtkastPage = function() {
    'use strict';

    var smittskyddLabel = element(by.css('[key="fk7263.label.smittskydd"]')),
        smittskyddCheckbox = element(by.id('smittskydd')),

        nedsattMed25Checkbox = element(by.id('nedsattMed25')),

        signeraButton = element(by.id('signera-utkast-button')),

        travelRadioButtonJa = element(by.id('rekommendationRessatt')),
        travelRadioGroupChecked = element(by.css('input[name="recommendationsToFkTravel"]:checked'));


    this.get = function() {
        browser.get('http://www.angularjs.org');
    };

    this.whenSmittskyddIsDisplayed = function() {
        return browser.wait(smittskyddLabel.isDisplayed());
    };

    this.getSmittskyddLabelText = function(){
        return smittskyddLabel.getText();
    };

    this.smittskyddCheckboxClick = function(){
        smittskyddCheckbox.click();
    };

    this.nedsattMed25CheckboxClick = function(){
        nedsattMed25Checkbox.click();
    };

    this.travelRadioButtonJaClick = function(){
        travelRadioButtonJa.click();
    };

    this.getCheckedTravelRadioButtonValue = function(){
        return travelRadioGroupChecked.getAttribute('value');
    };

    this.whenSigneraButtonIsEnabled = function(){
        return browser.wait(signeraButton.isEnabled());
    };

    this.signeraButtonClick = function(){
        signeraButton.click();
    };

};

module.exports = UtkastPage;
