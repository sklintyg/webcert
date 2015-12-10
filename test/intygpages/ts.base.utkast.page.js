/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by,browser,helpers*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

var BaseTsUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.intygType = null; // overridden by children

        this.korkortsTyperChecks = element(by.id('intygetAvserForm')).all(by.css('label.checkbox'));

        this.identitetForm = element(by.id('identitetForm'));

        this.bedomning = {
            form: element(by.id('bedomningForm')),
            yes: element(by.id('bedomningy')),
            no: element(by.id('bedomningn'))
        };
        this.bedomningKorkortsTyperChecks = this.bedomning.form.all(by.css('label.checkbox'));

        this.kommentar = element(by.id('kommentar'));
    },
    get: function get(intygId) {
        get._super.call(this, this.intygType, intygId);
    },
    fillInKorkortstyper: function(typer) {
        console.log('Anger körkortstyper: '+ typer.toString());
        helpers.page.clickAll(this.korkortsTyperChecks, typer);
    },
    fillInIdentitetStyrktGenom: function(idtyp) {
        console.log('Anger identitet styrkt genom ' + idtyp);
        this.identitetForm.element(by.cssContainingText('label.radio', idtyp)).sendKeys(protractor.Key.SPACE);
    },
    fillInBedomning: function(bedomningObj) {
        console.log('Anger bedömning: ' + bedomningObj.stallningstagande);
        element(by.id(bedomningObj.stallningstagande)).sendKeys(protractor.Key.SPACE);
        helpers.page.clickAll(this.bedomningKorkortsTyperChecks, bedomningObj.behorigheter);
    },
    fillInOvrigKommentar: function(utkast) {
        this.kommentar.sendKeys(utkast.kommentar);
    }
});

module.exports = BaseTsUtkast;
