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

/*global browser, intyg, logg,wcTestTools */
'use strict';

// function stringStartWith (string, prefix) {
//     return string.slice(0, prefix.length) === prefix;
// }
var testdataHelper = wcTestTools.helpers.testdata;

module.exports = function() {
    
    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, function(intygstyp, status, callback) {
        var qaTable = element(by.css('table.table-qa'));
        qaTable.all(by.cssContainingText('tr', intygstyp)).filter(function(elem, index) {
            return elem.getText().then(function(text) {
                return (text.indexOf(status) > -1);
            });
        }).then(function(filteredElements) {

            //Om det inte finns några intyg att använda
            if(!filteredElements[0]){
                createIntygWithStatus(intygstyp,status,callback);
                //Gå in på intyg
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


function createIntygWithStatus(typ,status,cb){
    //TODO, FUNKTION EJ KLAR
    cb('TODO: Hantera fall då det inte redan finns något intyg att använda');


    intyg.id = testdataHelper.generateTestGuid();
    console.log('intyg.id = '+ intyg.id);

    if(typ === 'Transportstyrelsens läkarintyg' && status === 'Signerat'){
        testdataHelper.createIntygFromTemplate('ts-bas', intyg.id).then(function(response) {
            console.log(response.request.body);

        }, function(error) {
            cb(error);
        }).then(cb);
}
    else{
        cb('TODO: Hantera fall då det inte redan finns något intyg att använda');
    }                      
}