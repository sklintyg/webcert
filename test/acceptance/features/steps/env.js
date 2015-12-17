/* globals browser */
'use strict';
var fs = require('fs');
module.exports = function() {
    this.setDefaultTimeout(100 * 1000);

    // //Before scenario
    // this.Before(function(scenario) {
    //     logg('before');
    // });

    //After scenario
    this.After(function(scenario, callback) {
        if (scenario.isFailed()) {
            logg('scenario failed');
            browser.takeScreenshot().then(function(png) {
                //var base64Image = new Buffer(png, 'binary').toString('base64');
                var decodedImage = new Buffer(png, 'base64').toString('binary');
                scenario.attach(decodedImage, 'image/png', function(err) {
                    callback(err);
                });
            });
        } else {
            callback();
        }
    });

    this.Before(function(scenario, callback) {

        global.scenario = scenario;
        callback();
    });

        global.logg = function(text){
            console.log(text);
            if(global){
                global.scenario.attach(text);
            }
        };

};