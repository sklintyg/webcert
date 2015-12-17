/*global
browser, intyg, logg
*/
'use strict';

function stringStartWith (string, prefix) {
    return string.slice(0, prefix.length) === prefix;
}

module.exports = function() {
    
    this.Given(/^jag går in på ett "([^"]*)" med status "([^"]*)"$/, function(intygstyp, status, callback) {
        var qaTable = element(by.css('table.table-qa'));

        qaTable.all(by.cssContainingText('tr', intygstyp)).filter(function(elem, index) {
            return elem.getText().then(function(text) {
                return (text.indexOf(status) > -1);
            });
        }).then(function(filteredElements) {
            if(!filteredElements[0]){
                callback('TODO: Hantera fall då det inte redan finns något intyg att använda');
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
