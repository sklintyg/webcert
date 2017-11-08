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

/*globals element,by, Promise*/
'use strict';

var BaseSkvUtkast = require('../skv.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');
var ElementArrayFinder = $$('').constructor;

//TODO flytta till common-testtools om funktionen gör det lättare att skriva mer lättlästa tester. Alternativt implementera protractor-helpers.
ElementArrayFinder.prototype.getByText = function (compareText) {
    var foundElement;
    return this.each(function (element) {
        element.getWebElement().getText().then(function (elementText) {
            if (elementText.trim() === compareText) {
                foundElement = element;
            }
        });
    }).then(function () {
        return foundElement;
    });
};



var DbUtkast = BaseSkvUtkast._extend({
    init: function init() {
        init._super.call(this);
	    this.identitetStyrktGenom = { //identitetStyrktGenom är inte samma element som i TS intyg
			container : element(by.id('form_identitetStyrkt')),
			inputText : element(by.id('identitetStyrkt'))
		}	   
		this.dodsdatum = {
			container : element(by.id('form_dodsdatumSakert')),
			sakert : {
				checkbox : element(by.id('dodsdatumSakertYes')),
				datepicker : 'id missing'
			},
			inteSakert : {
				checkbox : element(by.id('dodsdatumSakertNo')),
				dodsdatumMonth : element(by.id('dodsdatum-month')), //.ui-select-match.btn 
				dodsdatumYear : element(by.id('dodsdatum-year')) //.ui-select-match.btn 
			}
		}
		this.dodsplats = {
			kommun : {
				container : element(by.id('form_dodsplatsKommun')),
				inputText : element(by.id('dodsplatsKommun'))
			},
			boende : {
				container : element(by.id('form_dodsplatsBoende')),
				sjukhus : element(by.id('dodsplatsBoende-SJUKHUS')),
				ordinartBoende : element(by.id('dodsplatsBoende-ORDINART_BOENDE')),
				sarskiltBoende : element(by.id('dodsplatsBoende-SARSKILT_BOENDE')),
				annan : element(by.id('dodsplatsBoende-ANNAN'))
			}
		}
		this.barn = {
			container : element(by.id('form_barn')),
			ja : element(by.id('barnYes')),
			nej : element(by.id('barnNo'))
		}
		this.explosivImplantat = {
			container : element(by.id('form_explosivImplantat')),
			ja: element(by.id('explosivImplantatYes')),
			nej: element(by.id('explosivImplantatNo')),
			avlagsnat : {
				ja : element(by.id('explosivAvlagsnatYes')),
				nej : element(by.id('explosivAvlagsnatNo'))
			}
		}
		this.yttreUndersokning = {
			container : element(by.id('form_undersokningYttre')),
			ja : element(by.id('undersokningYttre-JA')),
			nejUndersokningSkaGoras : element(by.id('undersokningYttre-UNDERSOKNING_SKA_GORAS')),
			nejUndersokningGjortKortFore : {
				checkbox :  element(by.id('undersokningYttre-UNDERSOKNING_GJORT_KORT_FORE_DODEN')),
				datePicker : 'id missing'
			}
		}
		this.polisanmalan = {
			container : element(by.id('form_polisanmalan')),
			ja : element(by.id('polisanmalanYes')),
			nej : element(by.id('polisanmalanNo'))
		}
	},	
	angeIdentitetStyrktGenom : function angeIdentitetStyrktGenom(identitetStyrktGenom){
		var identitetStyrktGenomElm = this.identitetStyrktGenom;
		
		console.log(identitetStyrktGenomElm + ', ' + identitetStyrktGenom);
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angeDodsdatum : function angeDodsdatum(dodsdatum) {
		var dodsdatumElm = this.dodsdatum;
		
		console.log(dodsdatumElm + ', ' + dodsdatum)
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angeDodsPlats : function angeDodsPlats(dodsPlats) {
		var dodsPlatsElm = this.dodsPlats;
		
		console.log(dodsPlatsElm + ', ' + dodsPlats);
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angeBarn : function angeBarn(barn) {
		var barnELm = this.barn;
		
		console.log(barnElm + ', ' + barn)
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angeExplosivImplantat : function angeExplosivImplantat(explosivImplantat){
		var explosivImplantatElm = this.explosivImplantat;
		
		console.log(explosivImplantatElm + ', ' + explosivImplantat); 
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angeYttreUndersokning : function angeYttreUndersokning(yttreUndersokning){
		var yttreUndersokningElm = this.yttreUndersokning;
		
		console.log(yttreUndersokningElm + ', ' + yttreUndersokning);
		return new Promise(function(resolve) {
            resolve();
        });
	},
	angePolisanmalan : function angePolisanmalan(polisanmalan){
		var polisanmalanElm = this.polisanmalan;
		
		console.log(polisanmalanElm + ',' + polisanmalan);
		return new Promise(function(resolve) {
            resolve();
        });
	}
	
});

module.exports = new DbUtkast();
