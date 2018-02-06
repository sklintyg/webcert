/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by bennysce on 09/06/15.
 */
/*globals element,by, protractor, Promise*/
'use strict';

var pageHelpers = require('../../pageHelper.util.js');
var BaseUtkast = require('../base.utkast.page.js');

var bedomning = {
    form: element(by.id('form_bedomning')),
    yes: element(by.id('bedomning-lamplighetInnehaBehorighetYes')),
    no: element(by.id('bedomning-lamplighetInnehaBehorighetNo'))
};

var BaseTsUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.intygType = null; // overridden by children

        this.korkortsTyperChecks = element(by.id('form_intygAvser-korkortstyp')).all(by.css('label.big-checkbox-label'));

        this.identitetForm = element(by.id('form_vardkontakt-idkontroll'));
        this.specialist = element(by.id('bedomning-lakareSpecialKompetens'));

        this.bedomning = bedomning;

        this.bedomningKorkortsTyperChecks = this.bedomning.form.all(by.css('label.big-checkbox-label'));

        this.kommentar = element(by.id('kommentar'));
        this.adress = {
            postadress: element(by.id('patientPostadress')),
            postort: element(by.id('patientPostort')),
            postnummer: element(by.id('patientPostnummer'))

        };

        this.markeraKlartForSigneringButton = element(by.id('markeraKlartForSigneringButton'));
        this.markeraKlartForSigneringModalYesButton = element(by.id('buttonYes'));
        this.markeradKlartForSigneringText = element(by.id('draft-marked-ready-text'));

    },
    get: function get(intygId) {
        get._super.call(this, this.intygType, intygId);
    },
    fillInKorkortstyper: function(typer) {
        return pageHelpers.clickAll(this.korkortsTyperChecks, typer);
    },
    fillInIdentitetStyrktGenom: function(idtyp) {
        return this.identitetForm.element(by.cssContainingText('label.big-radio-label', idtyp)).sendKeys(protractor.Key.SPACE);
    },
    fillInBedomningLamplighet: function(lamplighet) {
        if (lamplighet) {
            if (lamplighet === 'Ja') {
                return bedomning.yes.sendKeys(protractor.Key.SPACE);
            } else {
                return bedomning.no.sendKeys(protractor.Key.SPACE);
            }
        }
        return Promise.resolve('Inget svar på lämplighet angivet');
    },
    fillInBedomning: function(bedomningObj) {
        var fillInLamplighet = this.fillInBedomningLamplighet;
        var bedomningKorkortsTyperChecks = this.bedomningKorkortsTyperChecks;

        return element(by.cssContainingText('label', bedomningObj.stallningstagande)).sendKeys(protractor.Key.SPACE)
            .then(function() {
                return pageHelpers.clickAll(bedomningKorkortsTyperChecks, bedomningObj.behorigheter)
                    .then(function() {
                        return fillInLamplighet(bedomningObj.lamplighet);
                    });
            });
    },
    fillInOvrigKommentar: function(utkast) {
        return this.kommentar.sendKeys(utkast.kommentar);
    },
    fillInSpecialist: function(specialist) {
        return this.specialist.sendKeys(specialist);
    },
    isMarkeraSomKlartAttSigneraButtonDisplayed: function() {
        return this.markeraKlartForSigneringButton.isDisplayed();
    },
    markeraSomKlartAttSigneraButtonClick: function() {
        this.markeraKlartForSigneringButton.sendKeys(protractor.Key.SPACE);
    }
});

module.exports = BaseTsUtkast;
