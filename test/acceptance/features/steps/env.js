/* globals browser */
'use strict';
var fs = require('fs');
module.exports = function() {
    this.setDefaultTimeout(100 * 1000);

    //Before scenario
    this.Before(function(scenario) {
        console.log('before');
    });

    //After scenario
    this.After(function(scenario) {
    	if(scenario.isFailed()){
    		console.log('scenario failed');
	        browser.takeScreenshot().then(function(png) {
	            //var base64Image = new Buffer(png, 'binary').toString('base64');
	            var decodedImage = new Buffer(png, 'base64').toString('binary');
	            scenario.attach(decodedImage, 'image/png');
	        });
    	}
    });
};