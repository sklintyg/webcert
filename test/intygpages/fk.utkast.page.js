/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,browser*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

var FkUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));
        this.smittskyddLabel = element(by.css('[key="fk7263.label.smittskydd"]'));
        this.smittskyddCheckbox = element(by.id('smittskydd'));

        this.nedsattMed25Checkbox = element(by.id('nedsattMed25'));

        this.signeraButton = element(by.id('signera-utkast-button'));

        this.travelRadioButtonJa = element(by.id('rekommendationRessatt'));
        this.travelRadioGroupChecked = element(by.css('input[name="recommendationsToFkTravel"]:checked'));

        this.capacityForWorkForecastText = element(by.id('capacityForWorkForecastText'));
    },
    get: function get(intygId) {
        get._super.call(this, 'fk7263', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    },
    whenSmittskyddIsDisplayed: function() {
        return browser.wait(this.smittskyddLabel.isDisplayed());
    },
    getSmittskyddLabelText: function() {
        return this.smittskyddLabel.getText();
    },
    smittskyddCheckboxClick: function() {
        this.smittskyddCheckbox.sendKeys(protractor.Key.SPACE);
    },
    nedsattMed25CheckboxClick: function() {
        this.nedsattMed25Checkbox.sendKeys(protractor.Key.SPACE);
    },
    travelRadioButtonJaClick: function() {
        this.travelRadioButtonJa.sendKeys(protractor.Key.SPACE);
    },
    getCheckedTravelRadioButtonValue: function() {
        return this.travelRadioGroupChecked.getAttribute('value');
    },
    getCapacityForWorkForecastText: function() {
        return this.capacityForWorkForecastText;
    }
});

module.exports = new FkUtkast();
