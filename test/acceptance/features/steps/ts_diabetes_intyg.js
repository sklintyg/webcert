/*global
browser, intyg, protractor
*/
'use strict';

function stringStartWith (string, prefix) {
    return string.slice(0, prefix.length) === prefix;
}

module.exports = function() {
	var qaTable = element(by.css('table.table-qa'));
    
    this.Given(/^och går in på ett "([^"]*)" med status "([^"]*)"$/, function(intygstyp, status, callback) {
    	qaTable.all(by.cssContainingText('tr',intygstyp)).each(function(el, index) {
		  
          //Leta efter intyg med status och klicka på visa-knapp
		  el.getText().then(function (text) {
		  		if(text.indexOf(status) > -1){
		  			el.element(by.cssContainingText('button','Visa')).click();
		  			callback();
		  		}
		        
		  });
		});
	});

    this.Given(/^jag skickar intyget till Transportstyrelsen$/, function(callback) {

    	//Fånga intygets id
    	if (!global.intyg){ global.intyg = {};}
    	browser.getCurrentUrl().then(function(text){
    		intyg.id = text.split('/').slice(-1)[0];
    		console.log('Intygsid: '+intyg.id);
    	});

        element(by.id('sendBtn')).click();
        element(by.id('patientSamtycke')).click();
        element(by.id('button1send-dialog')).click();
        callback();
    });

    this.Given(/^jag går till Mina intyg för patienten "([^"]*)"$/, function(pnr, callback) {
        var EC = protractor.ExpectedConditions;
        var url = process.env.MINAINTYG_URL + '/web/sso?guid=' + pnr;

        var urlChanged = function() {
            return browser.getCurrentUrl().then(function (currUrl) {
                return stringStartWith(currUrl, process.env.MINAINTYG_URL + '/web/start');
            });
        };
        
        var cond = EC.and(urlChanged);

        browser.get(url);
        browser.wait(cond, 50000);
        callback();
        
    });

    this.Given(/^ska intygets status i mvk visa "([^"]*)"$/, function(status, callback) {
        // Write code here that turns the phrase above into concrete actions
        var certBox = element(by.id('certificate-#'+intyg.id));
        expect(certBox.element(by.cssContainingText('.ng-binding', status))
        .isPresent()).to.eventually.to.equal(true)
        .and.notify(callback);

    });
};
