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

/* globals browser, logg */
'use strict';
var sleep = require('sleep');

var db = require('./db.js');

module.exports = function() {
    this.setDefaultTimeout(100 * 1000);

    function removeCert(intygsId,cb) {
        if (!intygsId) {
            logg('Intygsid saknas');
            cb();
        }
	else {
            var databaseTableINTYG = process.env.DATABASE_NAME + '.INTYG';
            var databaseTableSIGNATUR = process.env.DATABASE_NAME + '.SIGNATUR';
            var foreignKeyChecks0 = 'SET FOREIGN_KEY_CHECKS = 0;';
            var foreignKeyChecks1 = 'SET FOREIGN_KEY_CHECKS = 1;';

            var query1 = foreignKeyChecks0 + 'DELETE ' + databaseTableINTYG + ' FROM ' + databaseTableINTYG +
                ' INNER JOIN ' + databaseTableSIGNATUR + ' ON ' + databaseTableINTYG + '.INTYGS_ID=' + databaseTableSIGNATUR + '.INTYG_ID' +
                ' WHERE ' + databaseTableINTYG + '.INTYGS_ID="' + intygsId + '";' + foreignKeyChecks1;

            var CERTIFICATE = 'intyg_ip40.CERTIFICATE';
            var ORIGINAL_CERTIFICATE = 'intyg_ip40.ORIGINAL_CERTIFICATE';
            var CERTIFICATE_STATE = 'intyg_ip40.CERTIFICATE_STATE';

            var query2 = foreignKeyChecks0 + ' DELETE ' + CERTIFICATE + ' FROM ' + CERTIFICATE + ' INNER JOIN ' + ORIGINAL_CERTIFICATE +
                ' ON ' + CERTIFICATE + '.ID=' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID' + ' INNER JOIN ' + CERTIFICATE_STATE +
                ' ON ' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID=' + CERTIFICATE_STATE + '.CERTIFICATE_ID' +
                ' WHERE ' + CERTIFICATE + '.ID="' + intygsId + '"; ' + foreignKeyChecks1;

            var querys = [query2, query1];
            querys.forEach(function(q) {
                var conn = db.makeConnection();
                conn.connect();
                conn.query(q,
                    function(err, rows, fields) {
                        conn.end();
                        if (typeof rows !== 'undefined') {
                            logg(rows[0].affectedRows + ' row(s) affected.');
                            cb();
                        }
                        if (err) {
                             cb(err);
                        }

                    });
            });
        }
    }

    //After scenario
    this.After(function(scenario, callback) {
        callback();

        //Ska intyg rensas bort efter scenario?
        var rensaBortIntyg = true;
        var tagArr = scenario.getTags();
        for (var i = 0; i < tagArr.length; i++) {
            if(tagArr[i].getName() === '@keepIntyg'){
                rensaBortIntyg = false;
            }
        }

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

            if(process.env.DATABASE_PASSWORD && rensaBortIntyg){
                removeCert(global.intyg.id, callback);
            }
            else{
                logg('Behåller skapat testintyg');
                callback();
            }    
        }
    });

    this.Before(function(scenario, callback) {
        global.scenario = scenario;
        callback();
    });

    global.logg = function(text) {
        console.log(text);
        if (global) {
            global.scenario.attach(text);
        }
    };

};
