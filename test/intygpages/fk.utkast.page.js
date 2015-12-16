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
        this.aktuelltSjukdomsForlopp = element(by.id('diseaseCause'));
        this.arbetsformagaFMB = element(by.id('capacityForWorkText'));
        this.prognos= {
            JA: element(by.id('capacityForWork1')),
            JA_DELVIS: element(by.id('capacityForWork2')),
            NEJ: element(by.id('capacityForWork3')),
            GAR_EJ_ATT_BEDOMA: element(by.id('capacityForWork4')),
            fortydligande: element(by.id('capacityForWorkForecastText'))
        };

        this.nedsatt = {
            med25:{
                checkbox: element(by.id('nedsattMed25')),
                from: element(by.id('nedsattMed25from')),
                tom: element(by.id('nedsattMed25tom'))
            },
            med50:{
                checkbox: element(by.id('nedsattMed50')),
                from: element(by.id('nedsattMed50from')),
                tom: element(by.id('nedsattMed50tom'))
            },
            med75:{
                checkbox: element(by.id('nedsattMed75')),
                from: element(by.id('nedsattMed75from')),
                tom: element(by.id('nedsattMed75tom'))
            },
            med100:{
                checkbox: element(by.id('nedsattMed100')),
                from: element(by.id('nedsattMed100from')),
                tom: element(by.id('nedsattMed100tom'))
            }
        };
        this.baserasPa={
            minUndersokning:{
                checkbox:element(by.id('basedOnExamination')),
                datum:element(by.id('undersokningAvPatientenDate'))
            },
            minTelefonkontakt:{
                checkbox:element(by.id('basedOnPhoneContact')),
                datum:element(by.id('telefonkontaktMedPatientenDate'))
            },
            journaluppgifter:{
                checkbox:element(by.id('basedOnJournal')),
                datum:element(by.id('journaluppgifterDate'))
            },
            annat:{
                checkbox:element(by.id('basedOnOther')),
                datum:element(by.id('annanReferensDate')),
                text:element(by.id('informationBasedOnOtherText')) 
            }
        };

        this.diagnos = {
            fortydligande:element(by.id('diagnoseClarification')),
            samsjuklighetForeligger: element(by.id('diagnoseMultipleDiagnoses'))
        };
        
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
    angeDiagnosKod: function(kod) {
        this.diagnosKod.sendKeys(kod);
    },
    angeFunktionsnedsattning : function(txt) {
        this.funktionsNedsattning.sendKeys(txt);
    },
    angeAktivitetsBegransning : function(txt) {
        this.aktivitetsBegransning.sendKeys(txt);
    },
    angeNuvarandeArbete : function(txt) {
        this.nuvarandeArbete.sendKeys(txt);
    },
    angeFaktiskTjanstgoring : function(txt) {
        this.faktiskTjanstgoring.sendKeys(txt);
    },
    angeSmittskydd: function(isSmittskydd) {
        if(isSmittskydd){
            this.smittskyddCheckboxClick();
        }
    },
    angeIntygetBaserasPa:function(intygetBaserasPa){

        if(intygetBaserasPa.minUndersokning){
            this.baserasPa.minUndersokning.checkbox.click();
            this.baserasPa.minUndersokning.datum.sendKeys(intygetBaserasPa.minUndersokning.datum);
        }
        if(intygetBaserasPa.minTelefonkontakt){
            this.baserasPa.minTelefonkontakt.checkbox.click();
            this.baserasPa.minTelefonkontakt.datum.sendKeys(intygetBaserasPa.minTelefonkontakt.datum);
        }
        if(intygetBaserasPa.journaluppgifter){
            this.baserasPa.journaluppgifter.checkbox.click();
            this.baserasPa.journaluppgifter.datum.sendKeys(intygetBaserasPa.journaluppgifter.datum);
        }
        if(intygetBaserasPa.annat){
            this.baserasPa.annat.checkbox.click();
            this.baserasPa.annat.datum.sendKeys(intygetBaserasPa.annat.datum);
            this.baserasPa.annat.text.sendKeys(intygetBaserasPa.annat.text);
        }
    },
    angeDiagnoser:function(diagnos){
        if(diagnos.diagnoser){
            this.angeDiagnosKod(diagnos.diagnoser[0].ICD10);
        }
        if(diagnos.fortydligande){
            this.diagnos.fortydligande.sendKeys(diagnos.fortydligande);
        }
        if(diagnos.samsjuklighetForeligger){
            this.diagnos.samsjuklighetForeligger.sendKeys(diagnos.samsjuklighetForeligger);
        }
    },
    angeArbetsformaga:function(arbetsformaga){
        if(arbetsformaga.nedsattMed25){
            this.nedsatt.med25.checkbox.click();
            this.nedsatt.med25.from.sendKeys(arbetsformaga.nedsattMed25.from);
            this.nedsatt.med25.tom.sendKeys(arbetsformaga.nedsattMed25.tom);
        }
        if(arbetsformaga.nedsattMed50){
            this.nedsatt.med50.checkbox.click();
            this.nedsatt.med50.from.sendKeys(arbetsformaga.nedsattMed50.from);
            this.nedsatt.med50.tom.sendKeys(arbetsformaga.nedsattMed50.tom);
        }
        if(arbetsformaga.nedsattMed75){
            this.nedsatt.med75.checkbox.click();
            this.nedsatt.med75.from.sendKeys(arbetsformaga.nedsattMed75.from);
            this.nedsatt.med75.tom.sendKeys(arbetsformaga.nedsattMed75.tom);
        }
        if(arbetsformaga.nedsattMed100){
            this.nedsatt.med100.checkbox.click();
            this.nedsatt.med100.from.sendKeys(arbetsformaga.nedsattMed100.from);
            this.nedsatt.med100.tom.sendKeys(arbetsformaga.nedsattMed100.tom);
        }
    },
    angeAktuelltSjukdomsForlopp:function(txt){
        if(txt){this.aktuelltSjukdomsForlopp.sendKeys(txt);}
    },
    angeArbetsformagaFMB:function(txt){
        this.arbetsformagaFMB.sendKeys(txt);
    },
    angePrognos:function(prognos){
        if(prognos.choice.JA){this.prognos.sendKeys(protractor.Key.SPACE);}
        if(prognos.choice.JA_DELVIS){this.prognos.sendKeys(protractor.Key.SPACE);}
        if(prognos.choice.NEJ){this.prognos.sendKeys(protractor.Key.SPACE);}
        if(prognos.choice.GAR_EJ_ATT_BEDOMA){
            this.prognos.GAR_EJ_ATT_BEDOMA.sendKeys(protractor.Key.SPACE);
            if(prognos.fortydligande){
                this.prognos.fortydligande.sendKeys(prognos.fortydligande);
            }
        }
    }
});

module.exports = new FkUtkast();
