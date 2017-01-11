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

/* globals browser, logger */
'use strict';


function checkConsoleErrors(cb) {
    browser.manage().logs().get('browser').then(function(browserLog) {
        if (browserLog.length) {
            browserLog.forEach(function(log) {
                var error = log.level.value > 900;
                if (error) {
                    console.log(log);
                    throw ('KONSOL -> ' + log.level.name + ': ' + log.message);
                }
                cb();
            });
        }
    });
}

module.exports = function() {
    this.setDefaultTimeout(300 * 1000);
    global.externalPageLinks = [];

    this.AfterStep(function(event, callback) {

        // Samla in alla externa länkar på aktuell sida
        element.all(by.css('a')).each(function(link) {
            link.getAttribute('href').then(function(href) {
                if (href !== null &&
                    href !== '' &&
                    href.includes('javascript') !== true &&
                    href.indexOf(process.env.WEBCERT_URL) === -1 &&
                    href.indexOf(process.env.MINAINTYG_URL) === -1 &&
                    global.externalPageLinks.indexOf(href) === -1) {
                    console.log('Found one: ' + href);
                    global.externalPageLinks.push(href);
                }
            });
        }).then(function() {
            callback();
        });

    });

    this.Before(function(scenario) {
        global.scenario = scenario;

        //Återställ globala variabler
        global.person = {};
        global.intyg = {};
        global.meddelanden = []; //{typ:'', id:''}
        global.user = {};
    });

    //After scenario
    this.After(function(scenario, callback) {

        console.log('Rensar local-storage');
        browser.executeScript('window.sessionStorage.clear();');
        browser.executeScript('window.localStorage.clear();');

        //Ska intyg rensas bort efter scenario?
        var rensaBortIntyg = true;
        var tagArr = scenario.getTags();
        for (var i = 0; i < tagArr.length; i++) {
            if (tagArr[i].getName() === '@keepIntyg') {
                rensaBortIntyg = false;
            }
        }

        if (scenario.isFailed()) {
            browser.takeScreenshot().then(function(png) {
                // var decodedImage = new Buffer(png, 'base64');
                // return scenario.attach(decodedImage, 'image/png');

                var decodedImage = new Buffer(png, 'base64').toString('binary');
                scenario.attach(decodedImage, 'image/png', function(err) {
                    checkConsoleErrors(callback);
                });

            });

        } else {
            checkConsoleErrors(callback);
        }

    });



    logger.on('logging', function(transport, level, msg, meta) {
        if (global.scenario) {
            global.scenario.attach(level + ': ' + msg);
        }
    });



};
