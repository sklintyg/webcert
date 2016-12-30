/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
        this.adress = {
            postadress: element(by.id('patientPostadress')),
            postort: element(by.id('patientPostort')),
            postnummer: element(by.id('patientPostnummer'))

        };

    },
    get: function get(intygId) {
        get._super.call(this, this.intygType, intygId);
    },
    fillInKorkortstyper: function(typer) {
        return pageHelpers.clickAll(this.korkortsTyperChecks, typer);
    },
    fillInIdentitetStyrktGenom: function(idtyp) {
        return this.identitetForm.element(by.cssContainingText('label.radio', idtyp)).sendKeys(protractor.Key.SPACE);
    },
    fillInBedomning: function(bedomningObj) {
        return Promise.all([
            element(by.cssContainingText('label', bedomningObj.stallningstagande)).sendKeys(protractor.Key.SPACE),
            pageHelpers.clickAll(this.bedomningKorkortsTyperChecks, bedomningObj.behorigheter)
        ]);
    },
    fillInOvrigKommentar: function(utkast) {
            return this.kommentar.sendKeys(utkast.kommentar);
        }
        // fillInPatientAdress: function(adressObj) {
        //     return Promise.all([
        //         this.adress.postadress.clear().sendKeys(adressObj.postadress),
        //         this.adress.postnummer.clear().sendKeys(adressObj.postnummer),
        //         this.adress.postort.clear().sendKeys(adressObj.postort)
        //     ]);

    // }
});

module.exports = BaseTsUtkast;
