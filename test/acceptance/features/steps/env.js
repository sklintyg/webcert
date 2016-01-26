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
var mysql = require('mysql');
module.exports = function() {
    this.setDefaultTimeout(100 * 1000);

    function makeConnection() {
        return mysql.createConnection({
            host  :     process.env.DATABASE_HOST,
            user  :     process.env.DATABASE_USER,
            password  : process.env.DATABASE_PASSWORD, 
            database  : process.env.DATABASE_NAME
        });
         
    }

    function removeCert(intygsId) {
        sleep.sleep(10);
        
        var databaseTableINTYG = process.env.DATABASE_NAME + '.INTYG';
        var databaseTableSIGNATUR = process.env.DATABASE_NAME + '.SIGNATUR';
        var foreignKeyChecks0 = 'SET FOREIGN_KEY_CHECKS = 0;';
        var foreignKeyChecks1 = 'SET FOREIGN_KEY_CHECKS = 1;';

        var query1 = 'SET FOREIGN_KEY_CHECKS =' + 0 + '; DELETE ' + databaseTableINTYG + ' FROM ' + databaseTableINTYG +
         ' INNER JOIN ' + databaseTableSIGNATUR + ' ON ' + databaseTableINTYG + '.INTYGS_ID=' + databaseTableSIGNATUR + '.INTYG_ID'+
         ' WHERE ' + databaseTableINTYG + '.INTYGS_ID="' + intygsId + '"; SET FOREIGN_KEY_CHECKS =' + 1 + ';';

        var CERTIFICATE = 'intyg_ip40.CERTIFICATE';
        var ORIGINAL_CERTIFICATE = 'intyg_ip40.ORIGINAL_CERTIFICATE';
        var CERTIFICATE_STATE = 'intyg_ip40.CERTIFICATE_STATE';

        var query2 = ' DELETE ' + CERTIFICATE + ' FROM ' + CERTIFICATE + ' INNER JOIN ' + ORIGINAL_CERTIFICATE +
         ' ON ' + CERTIFICATE +'.ID=' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID' + ' INNER JOIN ' + CERTIFICATE_STATE +
         ' ON ' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID=' + CERTIFICATE_STATE + '.CERTIFICATE_ID' + 
         ' WHERE ' + CERTIFICATE +'.ID="' + intygsId + '"; SET FOREIGN_KEY_CHECKS = 1;';

        // var querys = [query1 ,query2];

            var conn = makeConnection();
            console.log('Running: ' + query1);
            conn.connect();
            conn.query(query1,
                function(err, rows, fields) {
                    conn.end();
                    if (err) { throw err; }
                }); 
        // querys.forEach( function (q){ });

    }

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
            removeCert(global.intyg.id);
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
