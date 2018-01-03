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

/* globals browser, logger, JSON */
'use strict';


var fs = require('fs');

var hasFoundConsoleErrors = false;
var duplicateIds = [];

function writeScreenShot(data, filename, cb) {
    var stream = fs.createWriteStream(filename);
    stream.write(new Buffer(data, 'base64'));
    stream.end();
    stream.on('finish', cb);
}

function checkConsoleErrors() {
    if (hasFoundConsoleErrors) {

        // 500-error är ett godkänt fel i detta test, se INTYG-3524
        if (global.scenario.getName().indexOf('Kan byta vårdenhet') >= 0 && hasFoundConsoleErrors.indexOf('error 500') > -1) {
            logger.info('Hittade 500-fel. Detta fel är accepterat, se INTYG-3524');
            return;
        } else if (hasFoundConsoleErrors.indexOf('ID-dubletter') > -1) {
            logger.warn(hasFoundConsoleErrors);
            return;
        } else {
            logger.error(hasFoundConsoleErrors);
            throw ('Hittade script-fel under körning');
        }
    } else {
        return;
    }
}

function removeAlerts() {
    browser.switchTo().alert().accept()
        .then(() => logger.log('info', 'Dialogruta accepterad.'))
        .catch(err => {}); // Ingen dialogruta hittad, allt är frid och fröjd.
}

module.exports = function() {
    this.setDefaultTimeout(600 * 1000);
    global.externalPageLinks = [];

    this.AfterStep(function(event) {
        // Ibland dyker en dialogruta upp "du har osparade ändringar". Vi vill ignorera denna och gå vidare till nästa test.
        removeAlerts();

        return new Promise(function(resolve) {
            //Kör promisekedja för AfterStep.
            resolve();
        }).then(function() {
            // Samla in alla externa länkar på aktuell sida
            return element.all(by.css('a')).each(function(link) {
                return link.getAttribute('href').then(function(href) {
                    if (href !== null &&
                        href !== '' &&
                        href.includes('javascript') !== true &&
                        href.indexOf(process.env.WEBCERT_URL) === -1 &&
                        href.indexOf(process.env.MINAINTYG_URL) === -1 &&
                        href.indexOf(process.env.REHABSTOD_URL) === -1 &&
                        href.indexOf(process.env.STATISTIKTJANST_URL) === -1 &&
                        global.externalPageLinks.indexOf(href) === -1) {
                        console.log('Found one: ' + href);
                        global.externalPageLinks.push(href);
                    }
                });
            });
        }).then(function() {
            //Rapportera om ID-dubletter. Är inte rimligt att göra med protractor, kör front-end script istället.
            var frontEndScript = '';

            frontEndScript += 'if (window.jQuery) {';
            frontEndScript += 'var arr = [];';
            frontEndScript += '$("[id]").each(function(){';
            frontEndScript += 'var ids = $("[id]");';
            frontEndScript += 'if(ids.length>1 && ids[0]==this && this.id != "ng-app") {';
            frontEndScript += 'arr.push(this.id);}';

            frontEndScript += '});';
            frontEndScript += 'if (arr.length > 1) {';
            frontEndScript += 'console.error(arr.length + "st ID-dubletter Hittade, " + JSON.stringify(arr));'; //använder console.error så plockas det upp i nästa steg som kollar efter error.
            frontEndScript += '}}';

            return browser.getCurrentUrl().then(function() {
                //Browser is open
                return browser.executeScript(frontEndScript);
            }).catch(function() {
                //Browser was closed
                return;
            });
        }).then(function() {
            return browser.getCurrentUrl().then(function() {
                //Skriv ut script-fel, Kan inte kasta fel i AfterStep tyvärr
                return browser.executeScript('return window.errs;').then(function(v) {
                    if (v && v.length > 0) {
                        hasFoundConsoleErrors = JSON.stringify(v);

                        logger.error(hasFoundConsoleErrors);
                        return;
                    }
                });
            }).catch(function() {
                //Browser was closed
                return;
            });
        });

    });

    this.Before(function(scenario) {
        global.scenario = scenario;

        logger.info('Återställer globala variabler');
        global.person = {};
        global.intyg = {};
        global.meddelanden = []; //{typ:'', id:''}
        global.user = {};
        hasFoundConsoleErrors = false;
        duplicateIds = [];
    });
    //After scenario
    this.After(function(scenario) {

        console.log('Rensar session-storage');
        return browser.executeScript('window.sessionStorage.clear();').then(function() {
            console.log('Rensar local-storage');
            return browser.executeScript('window.localStorage.clear();');
        }).then(function() {

            if (scenario.isFailed()) {

                var frontEndJS = 'var div = document.createElement("DIV"); ';
                frontEndJS += 'div.style.position = "fixed";';
                frontEndJS += 'div.style.height = (window.innerHeight - 2) + "px";';
                frontEndJS += 'div.style.width = (window.innerWidth - 2) + "px";';
                frontEndJS += 'div.style.border = "1px solid red";';
                frontEndJS += 'div.style.top = "1px";';
                frontEndJS += 'div.style.zIndex = "10000";';
                frontEndJS += 'var body = document.getElementsByTagName("BODY")[0];';
                frontEndJS += 'body.appendChild(div);';

                return browser.executeScript(frontEndJS).then(function() {
                    return browser.takeScreenshot().then(function(png) {
                        var ssPath = './node_modules/common-testtools/cucumber-html-report/';
                        var filename = 'screenshots/' + new Date().getTime() + '.png';
                        return writeScreenShot(png, ssPath + filename, function() {
                            return scenario.attach(filename, 'image/png', function(err) {
                                if (err) {
                                    throw err;
                                }
                                console.log('Skärmbild tagen: ' + filename);
                                return checkConsoleErrors();
                            });
                        });
                    });
                });

            } else {
                return checkConsoleErrors();
            }

        });


        //Ska intyg rensas bort efter scenario? TODO: rensaBortIntyg används aldrig.
        /*var rensaBortIntyg = true;
        var tagArr = scenario.getTags();
        for (var i = 0; i < tagArr.length; i++) {
            if (tagArr[i].getName() === '@keepIntyg') {
                rensaBortIntyg = false;
            }
        }*/




    });



    logger.on('logging', function(transport, level, msg, meta) {
        if (global.scenario) {
            global.scenario.attach(level + ': ' + msg);
        }
    });



};
