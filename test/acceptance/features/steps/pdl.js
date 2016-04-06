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

/* globals intyg, logger, Promise, wcTestTools, JSON */
'use strict';
var db = require('./dbActions');
var fk7263IntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;




module.exports = function() {

    this.Given(/^ska loggaktivitet "([^"]*)" skickas till loggtjänsten$/, function(activity, callback) {
        var p3 = new Promise(function(resolve, reject) {
            setTimeout(resolve, 20000);
        });

        p3
            .then(function() {
                return fetchLogEntries(activity);
            })
            .then(function(result) {

                //     for (var i = 0; i <= result.length; i++) {
                //         callback();
                //     }
                //     
                if (result.length > 0) {
                    logger.info('Result:' + JSON.stringify(result));
                    callback();
                } else {
                    callback('Hittade inga rader databasen');
                }

            }, function(reason) {
                callback('FEL,' + reason);
            });
    });


    this.Given(/^jag skriver ut intyget$/, function(callback) {
        fk7263IntygPage.skrivUtFullstandigtIntyg().then(function() {
            callback();
        });
    });

    this.Given(/^ska det nu finnas (\d+) loggaktivitet "([^"]*)" för intyget$/, function(count, activity, callback) {
        var p3 = new Promise(function(resolve, reject) {
            setTimeout(resolve, 20000);
        });
        p3
            .then(function() {
                return fetchLogEntries(activity);
            })
            .then(function(result) {

                //     for (var i = 0; i <= result.length; i++) {
                //         callback();
                //     }
                //     
                if (result.length >= count) {
                    logger.info('Result:' + JSON.stringify(result));
                    callback();
                } else {
                    callback('Hittade färre än ' + count + ' rader databasen');
                }

            }, function(reason) {
                callback('FEL,' + reason);
            });
    });





    function fetchLogEntries(activity) {

        var p1 = new Promise(function(resolve, reject) {
            db.getLogEntries(activity, intyg.id, global.user.hsaId)
                .then(function(result) { //success
                    //do check
                    //
                    resolve(result);

                }, function(reason) { //err
                    reject(reason);
                });
        });

        return p1;

    }
};
