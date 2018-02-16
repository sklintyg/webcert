/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
/*jshint newcap:false */

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
            return logger.info('Hittade 500-fel. Detta fel är accepterat, se INTYG-3524');
        } else if (hasFoundConsoleErrors.indexOf('ID-dubletter') > -1) {
            return logger.warn(hasFoundConsoleErrors);
        } else {
            logger.error(hasFoundConsoleErrors);
            throw ('Hittade script-fel under körning');
        }
    } else {
        return logger.info('OK - Inga scriptfel hittades');
    }
}

function removeAlerts() {
    return browser.switchTo().alert().then(function() {
        logger.log('info', 'Dialogruta accepterad.');
        return;
    }, function(e) {
        // Ingen dialogruta hittad, allt är frid och fröjd.*/
        return;
    });
}

const {
    setDefaultTimeout, // jshint ignore:line
    Before, // jshint ignore:line
    BeforeAll, // jshint ignore:line
    After, // jshint ignore:line
    AfterAll // jshint ignore:line
} = require('cucumber');


setDefaultTimeout(600 * 1000);
global.externalPageLinks = [];

AfterAll(function() {

    logger.silly('Samlar in alla externa länkar på aktuell sida');
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
                    logger.info('Found external link: ' + href);
                    global.externalPageLinks.push(href);
                }
            }).catch(function(err) {
                logger.warn('Fel vid insamling av externa länkar');
                logger.debug(err);
                return;
            });
        }).catch(function(err) {
            logger.warn('Fel vid insamling av externa länkar');
            logger.debug(err);
            return;
        }).then(function() {
            logger.silly('Rapportera om ID-dubletter. Är inte rimligt att göra med protractor, kör front-end script istället.');
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
                logger.silly('Kontroll av ID-dubletter felade - Browser was closed');
                return;
            });
        }).then(function() {
            logger.silly('Skriv ut script-fel, Kan inte kasta fel i AfterStep tyvärr');
            return browser.getCurrentUrl().then(function(url) {
                logger.silly('current URL ' + url);
                //Skriv ut script-fel, Kan inte kasta fel i AfterStep tyvärr
                return browser.executeScript('return window.errs;').then(function(v) {
                    if (v && v.length > 0) {
                        hasFoundConsoleErrors = JSON.stringify(v);

                        logger.error(hasFoundConsoleErrors);
                        return;
                    }
                });
            }).catch(function() {
                logger.warn('Browser is closed.');
                return;
            });
        })
        .then(function() {
            // Ibland dyker en dialogruta upp "du har osparade ändringar". Vi vill ignorera denna och gå vidare till nästa test.
            return removeAlerts();
        });

});

Before(function() {
    global.scenario = this;

    logger.info('Återställer globala variabler');
    global.person = {};
    global.intyg = {};
    global.meddelanden = []; //{typ:'', id:''}
    global.user = {};
    hasFoundConsoleErrors = false;
    duplicateIds = [];
});



After(function(testCase) {

    var world = this;
    browser.ignoreSynchronization = true;

    if (testCase.result.status === 'failed') {

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
                return browser.takeScreenshot();
            }).then(function(png) {
                var ssPath = './node_modules/common-testtools/cucumber-html-report/';
                var filename = 'screenshots/' + new Date().getTime() + '.png';
                return writeScreenShot(png, ssPath + filename, function() {
                    return world.attach(new Buffer(png, 'base64'), 'image/png', function(err) {
                        //return world.attach(filename, 'image/png', function(err) {
                        if (err) {
                            throw err;
                        }
                        logger.silly('Skärmbild tagen: ' + filename);
                        return checkConsoleErrors();
                    });
                });
            }).then(function() {
                logger.silly('Rensar session-storage');
                return browser.executeScript('window.sessionStorage.clear();');
            }).then(function() {
                logger.silly('Rensar local-storage');
                return browser.executeScript('window.localStorage.clear();');
            })
            .then(function() {
                var url = 'about:blank';
                console.log('går till ' + url);
                return browser.get(url);
            })
            .then(function() {
                return browser.sleep(1000);
            })
            .then(function() {
                return removeAlerts();
            }).then(function() {
                return browser.sleep(1000);
            }).then(function() {
                console.log('browser.refresh');
                return browser.refresh();
            });

    } else {
        logger.silly('Rensar session-storage');
        return browser.executeScript('window.sessionStorage.clear();').then(function() {
                return checkConsoleErrors();
            }).then(function() {
                logger.silly('Rensar local-storage');
                return browser.executeScript('window.localStorage.clear();');
            }).then(function() {
                var url = 'about:blank';
                console.log('går till ' + url);
                return browser.get(url);
            })
            .then(function() {
                return removeAlerts();
            }).then(function() {
                return browser.sleep(1000);
            }).then(function() {
                return browser.refresh();
            });
    }
});



logger.on('logging', function(transport, level, msg, meta) {
    var date = new Date();
    var dateString = date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds() + ' ' + date.getMilliseconds();
    if (global.scenario) {
        global.scenario.attach(Buffer.from(dateString + ' - ' + level + ': ' + msg).toString('base64'));
    }
});
