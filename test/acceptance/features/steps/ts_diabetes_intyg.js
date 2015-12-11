/*global
browser, intyg
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
            filteredElements[0].element(by.cssContainingText('button', 'Visa')).click();
            callback();
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

    // this.Given(/^jag går till Mina intyg för patienten "([^"]*)"$/, function(pnr, callback) {
    //     // TODO / FIX THIS 

    //     var EC = protractor.ExpectedConditions;
    //     var url = process.env.MINAINTYG_URL + '/web/sso?guid=' + pnr;

    //     var urlChanged = function() {
    //         return browser.getCurrentUrl().then(function (currUrl) {
    //             return stringStartWith(currUrl, process.env.MINAINTYG_URL + '/web/start');
    //         });
    //     };
        
    //     var cond = EC.and(urlChanged);

    //     browser.get(url);
    //     browser.wait(cond, 50000);
    //     callback();
        
    // });

    // this.Given(/^ska intygets status i mvk visa "([^"]*)"$/, function(status, callback) {
    //     // Write code here that turns the phrase above into concrete actions
    //     var certBox = element(by.id('certificate-#'+intyg.id));
    //     expect(certBox.element(by.cssContainingText('.ng-binding', status))
    //     .isPresent()).to.eventually.to.equal(true)
    //     .and.notify(callback);

    // });
};
