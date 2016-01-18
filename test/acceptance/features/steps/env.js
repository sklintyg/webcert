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
