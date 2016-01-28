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

/*global
browser, intyg, logg
*/
'use strict';

// function stringStartWith (string, prefix) {
//     return string.slice(0, prefix.length) === prefix;
// }

module.exports = function() {
    
    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, function(intygstyp, status, callback) {
        var qaTable = element(by.css('table.table-qa'));
        qaTable.all(by.cssContainingText('tr', intygstyp)).filter(function(elem, index) {
            return elem.getText().then(function(text) {
                return (text.indexOf(status) > -1);
            });
        }).then(function(filteredElements) {
            if(!filteredElements[0]){
                var wcTestTools = require('webcert-testtools');
                var specHelper = wcTestTools.helpers.spec;
                var testdataHelper = wcTestTools.helpers.testdata;
                
                var intygId = specHelper.generateTestGuid();
                console.log('intygsId = '+ intygId);
                
                testdataHelper.createIntygFromTemplate('ts-bas', intygId).then(function(response) {
                    global.JSON.parse(response.request.body);
                }, function(error) {
                    console.log('Error calling createIntyg');
                }).then(callback);
                //callback('TODO: Hantera fall då det inte redan finns något intyg att använda');
            }
            else{
                filteredElements[0].element(by.cssContainingText('button', 'Visa')).click();
                callback();
            }
        });       
	});

    this.Given(/^jag skickar intyget till "([^"]*)"$/, function(dest, callback) {

    	//Fånga intygets id
    	if (!global.intyg){ global.intyg = {};}
    	browser.getCurrentUrl().then(function(text){
    		intyg.id = text.split('/').slice(-1)[0];
    		logg('Intygsid: '+intyg.id);
    	});

        element(by.id('sendBtn')).click();
        element(by.id('patientSamtycke')).click();
        element(by.id('button1send-dialog')).click();
        
        callback();
    });
};
