/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';

var at = element(by.id('edit-fk7263')),
    smittskyddLabel = element(by.css('[key="fk7263.label.smittskydd"]')),
    smittskyddCheckbox = element(by.id('smittskydd')),

    nedsattMed25Checkbox = element(by.id('nedsattMed25')),

    signeraButton = element(by.id('signera-utkast-button')),

    travelRadioButtonJa = element(by.id('rekommendationRessatt')),
    travelRadioGroupChecked = element(by.css('input[name="recommendationsToFkTravel"]:checked')),

    capacityForWorkForecastText = element(by.id('capacityForWorkForecastText'));

module.exports = {
    get: function(intygId) {
        browser.get('/web/dashboard#/fk7263/edit/' + intygId);
    },
    at: function() {
        return at.isDisplayed();
    },
    whenSmittskyddIsDisplayed: function() {
        return browser.wait(smittskyddLabel.isDisplayed());
    },
    getSmittskyddLabelText: function(){
        return smittskyddLabel.getText();
    },
    smittskyddCheckboxClick: function(){
        smittskyddCheckbox.click();
    },
    nedsattMed25CheckboxClick: function(){
        nedsattMed25Checkbox.click();
    },
    travelRadioButtonJaClick: function(){
        travelRadioButtonJa.click();
    },
    getCheckedTravelRadioButtonValue: function(){
        return travelRadioGroupChecked.getAttribute('value');
    },
    whenSigneraButtonIsEnabled: function(){
        return browser.wait(signeraButton.isEnabled());
    },
    signeraButtonClick: function(){
        signeraButton.click();
    },
    capacityForWorkForecastText: function(){
        return capacityForWorkForecastText;
    }
};
