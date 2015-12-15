/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,browser, protractor*/
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
        this.minUndersokning = element(by.id('basedOnExamination'));
        this.diagnosKod = element(by.id('diagnoseCode'));
        this.funktionsNedsattning = element(by.id('disabilities'));
        this.aktivitetsBegransning = element(by.id('activityLimitation'));
        this.nuvarandeArbete = element(by.id('currentWork'));
        this.faktiskTjanstgoring = element(by.id('capacityForWorkActualWorkingHoursPerWeek'));

        this.nedsattMed50 = element(by.id('nedsattMed50'));
        this.nedsattMed50from = element(by.id('nedsattMed50from'));
        this.nedsattMed50tom = element(by.id('nedsattMed50tom'));
        
        
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
    },
    minUndersokningAvPatClick: function() {
        this.minUndersokning.sendKeys(protractor.Key.SPACE);
    },
    angeDiagnosKod: function angeDiagnosKod(kod) {
        this.diagnosKod.sendKeys(kod);
    },
    angeFunktionsnedsattning : function angeFunktionsnedsattning(txt) {
        this.funktionsNedsattning.sendKeys(txt);
    },
    angeAktivitetsBegransning : function angeAktivitetsBegransning(txt) {
        this.aktivitetsBegransning.sendKeys(txt);
    },
    angeNuvarandeArbete : function angeNuvarandeArbete(txt) {
        this.nuvarandeArbete.sendKeys(txt);
    },
    angeFaktiskTjanstgoring : function angeFaktiskTjanstgoring(txt) {
        this.faktiskTjanstgoring.sendKeys(txt);
    },
    angeSmittskydd: function(isSmittskydd) {
        if(isSmittskydd){
            this.smittskyddCheckboxClick();
        }
    },
    angeArbetsformaga:function(arbetsformaga){
        if(arbetsformaga.nedsattMed50){
            this.nedsattMed50.click();
            this.nedsattMed50from.sendKeys(arbetsformaga.nedsattMed50.from);
            this.nedsattMed50tom.sendKeys(arbetsformaga.nedsattMed50.tom);
        }
    }
});

module.exports = new FkUtkast();
