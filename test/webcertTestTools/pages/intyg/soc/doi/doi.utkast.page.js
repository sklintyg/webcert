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

/*globals element,by, Promise, protractor*/
'use strict';

var BaseSocUtkast = require('../soc.base.utkast.page.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init();

var moveAndSendKeys = testTools.uiHelpers.moveAndSendKeys;
var scrollElm = testTools.uiHelpers.scrollElement;

var doiUtkast = BaseSocUtkast._extend({
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
				datePicker : element(by.id('datepicker_dodsdatum'))
			},
			inteSakert : {
				checkbox : element(by.id('dodsdatumSakertNo')),
				month : element(by.css('#dodsdatum-month > div.ui-select-match > span.btn > span.ui-select-match-text > span.ng-binding')),
				year : element(by.css('#dodsdatum-year > div.ui-select-match > span.btn > span.ui-select-match-text > span.ng-binding')),
				options : element.all(by.css('.ui-select-choices-row-inner')),
				antraffadDod : element(by.id('datepicker_antraffatDodDatum'))
			}
		}
		this.dodsPlats = {
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
		this.enhetensAdress = {
            postAdress: element(by.id('grundData.skapadAv.vardenhet.postadress')),
            postNummer: element(by.id('grundData.skapadAv.vardenhet.postnummer')),
            postOrt: element(by.id('grundData.skapadAv.vardenhet.postort')),
            enhetsTelefon: element(by.id('grundData.skapadAv.vardenhet.telefonnummer'))
        }
	},	
	angeIdentitetStyrktGenom : function angeIdentitetStyrktGenom(identitetStyrktGenom){
		var identitetStyrktGenomElm = this.identitetStyrktGenom.inputText;
		
		return moveAndSendKeys(identitetStyrktGenomElm, identitetStyrktGenom);
	},
	angeDodsdatum : function angeDodsdatum(dodsdatum) {
		var dodsdatumElm = this.dodsdatum;
		
		console.log(dodsdatum.sakert);

        if (dodsdatum.sakert) {
			return moveAndSendKeys(dodsdatumElm.sakert.checkbox, protractor.Key.SPACE).then(function(){
				return moveAndSendKeys(dodsdatumElm.sakert.datePicker, dodsdatum.sakert.datum);
			});
		} else {
			console.log(dodsdatum.inteSakert);
			return moveAndSendKeys(dodsdatumElm.inteSakert.checkbox, protractor.Key.SPACE)
			.then(function(){
				return dodsdatumElm.inteSakert.year.click().then(function(){			
					return dodsdatumElm.inteSakert.options.getByText(dodsdatum.inteSakert.year)					
					.then(function(elm){
						return elm.click();
					});
				});
			})
			.then(function(){
				if (dodsdatum.inteSakert.year !== '0000 (ej känt)') {
					return dodsdatumElm.inteSakert.month.click()
					.then(function() {
						return dodsdatumElm.inteSakert.options.getByText(dodsdatum.inteSakert.month).then(function(monthElm){
							return scrollElm(monthElm, 2)
							.then(function(){
								return monthElm.click();		
							})
							.then(function(){
								return moveAndSendKeys(dodsdatumElm.inteSakert.antraffadDod, dodsdatum.inteSakert.antraffadDod);
							});
						});
					});
				} return;
			});
		}
	},
	angeDodsPlats : function angeDodsPlats(dodsPlats) {
		var dodsPlatsElm = this.dodsPlats;
		
		return moveAndSendKeys(dodsPlatsElm.kommun.inputText, dodsPlats.kommun)
		.then(function(){
			switch (dodsPlats.boende) {
				case 'sjukhus':
					return moveAndSendKeys(dodsPlatsElm.boende.sjukhus, protractor.Key.SPACE);
					break;
				case 'ordinartBoende':
					return moveAndSendKeys(dodsPlatsElm.boende.ordinartBoende, protractor.Key.SPACE);
					break;
				case 'sarskiltBoende': 
					return moveAndSendKeys(dodsPlatsElm.boende.sarskiltBoende, protractor.Key.SPACE);
					break;
				case 'annan':
					return moveAndSendKeys(dodsPlatsElm.boende.annan, protractor.Key.SPACE);
					break;
				default:
					throw('dodsPlats.boende hittades inte');
			}	
		});
	},
	angeBarn : function angeBarn(barn) {
		var barnElm = this.barn;
		
		if (typeof barn !== 'undefined') {
			if (barn === true) {
				return moveAndSendKeys(barnElm.ja, protractor.Key.SPACE);
			} else {
				return moveAndSendKeys(barnElm.nej, protractor.Key.SPACE);
			}		
		} else {
			return Promise.resolve();
		}
		
	}
});

module.exports = new doiUtkast();
