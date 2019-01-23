/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
 * Created by bennysce on 09/12/15.
 */
/*globals element,by, protractor, Promise*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init();

var TsTrk1009Utkast = BaseTsUtkast._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'tstrk1009';
        this.intygTypeVersion = '1.0';
        this.at = element(by.id('edit-tstrk1009'));

        this.identitetStyrktGenom = element(by.id('form_identitetStyrktGenom-typ'));

        this.anmalanAvser = {
            OLAMPLIGHET: element(by.id('anmalanAvser.typ-OLAMPLIGHET')),
            SANNOLIK_OLAMPLIGHET: element(by.id('anmalanAvser.typ-SANNOLIK_OLAMPLIGHET'))
        }

        this.medicinskaForhallanden = element(by.id('medicinskaForhallanden'));
        this.senasteUndersokningsdatum = element(by.id('datepicker_senasteUndersokningsdatum'));

        this.intygetAvserBehorigheter = element(by.id('form_intygetAvserBehorigheter-typer')).all(by.css('label'));

        this.informationOmTsBeslutOnskas = element(by.id('informationOmTsBeslutOnskas'));
    },
    fillInIdentitetStyrktGenom1009: function(idtyp) {
        return this.identitetStyrktGenom.element(by.cssContainingText('label', idtyp)).click();
    },
    fillAnmalanAvser: function(anmalanAvser) {
        this.anmalanAvser[anmalanAvser].click();
    },
    fillMedicinskaForhallanden: function(medicinskaForhallanden, senasteUndersokningsdatum){
        this.medicinskaForhallanden.sendKeys(medicinskaForhallanden);
        this.senasteUndersokningsdatum.sendKeys(senasteUndersokningsdatum);
    },
    fillBehorigheter: function(behorigheter){
        return pageHelpers.selectAllCheckBoxes(this.intygetAvserBehorigheter, behorigheter);
    },
    fillInformationOmTsBeslutOnskas: function(informationOmTsBeslutOnskas) {
        if(informationOmTsBeslutOnskas){
            this.informationOmTsBeslutOnskas.click();
        }
    }
});

module.exports = new TsTrk1009Utkast();
