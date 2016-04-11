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

/* globals logger, wcTestTools, Promise */

'use strict';
var mysql = require('mysql');
var testdataHelper = wcTestTools.helpers.testdata;

function formatDate(date) {
    var time = ((date.getHours() < 10) ? '0' : '') +
        date.getHours() + ':' +
        ((date.getMinutes() < 10) ? '0' : '') +
        date.getMinutes() + ':' +
        ((date.getSeconds() < 10) ? '0' : '') + date.getSeconds();
    return testdataHelper.dateFormat(date) + 'T' + time;
}

module.exports = {
    makeConnection: function() {
        if (!process.env.DATABASE_PASSWORD) {
            throw 'Miljövariabel DATABASE_PASSWORD saknas';
        }
        return mysql.createConnection({
            host: process.env.DATABASE_HOST,
            user: process.env.DATABASE_USER,
            password: process.env.DATABASE_PASSWORD,
            database: process.env.DATABASE_NAME,
            multipleStatements: true
        });
    },
    getLogEntries: function(activity, intygsID, lakareID) {
        var conn = this.makeConnection();

        var dbTable = 'webcert_requests.storelog__mock_requests';
        // var oneMinuteSinceNow = '2017-03-17 06:42:10';
        var now = new Date();
        var oneMinuteSinceNow = new Date(now.getTime() + (-1) * 60000);
        oneMinuteSinceNow = formatDate(oneMinuteSinceNow);

        var query = 'SELECT * FROM ' + dbTable + ' where ' +
            ' activityLevel = "' + intygsID + '"' +
            ' AND activitytype = "' + activity + '"' +
            ' AND userid = "' + lakareID + '"' +
            ' AND logtime>="' + oneMinuteSinceNow + '"';

        logger.info('query: ' + query);
        var p1 = new Promise(function(resolve, reject) {
            conn.connect();
            conn.query(query,
                function(err, rows, fields) {
                    conn.end();
                    if (err) {
                        reject(err);
                    }
                    resolve(rows);
                });
        });
        return p1;
    },

    removeCert: function(intygsId, cb) {
        var envName = 'ip30';

        if (!intygsId) {
            logger.info('Intygsid saknas');
            cb();
        } else {
            var databaseTableINTYG = process.env.DATABASE_NAME + '.INTYG';
            var databaseTableSIGNATUR = process.env.DATABASE_NAME + '.SIGNATUR';
            var foreignKeyChecks0 = 'SET FOREIGN_KEY_CHECKS = 0;';
            var foreignKeyChecks1 = 'SET FOREIGN_KEY_CHECKS = 1;';

            var query1 = foreignKeyChecks0 + 'DELETE ' + databaseTableINTYG + ' FROM ' + databaseTableINTYG +
                ' INNER JOIN ' + databaseTableSIGNATUR + ' ON ' + databaseTableINTYG + '.INTYGS_ID=' + databaseTableSIGNATUR + '.INTYG_ID' +
                ' WHERE ' + databaseTableINTYG + '.INTYGS_ID="' + intygsId + '";' + foreignKeyChecks1;

            var CERTIFICATE = 'intyg_' + envName + '.CERTIFICATE';
            var ORIGINAL_CERTIFICATE = 'intyg_' + envName + '.ORIGINAL_CERTIFICATE';
            var CERTIFICATE_STATE = 'intyg_' + envName + '.CERTIFICATE_STATE';

            var query2 = foreignKeyChecks0 + ' DELETE ' + CERTIFICATE + ' FROM ' + CERTIFICATE + ' INNER JOIN ' + ORIGINAL_CERTIFICATE +
                ' ON ' + CERTIFICATE + '.ID=' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID' + ' INNER JOIN ' + CERTIFICATE_STATE +
                ' ON ' + ORIGINAL_CERTIFICATE + '.CERTIFICATE_ID=' + CERTIFICATE_STATE + '.CERTIFICATE_ID' +
                ' WHERE ' + CERTIFICATE + '.ID="' + intygsId + '"; ' + foreignKeyChecks1;

            var querys = [query2, query1];
            querys.forEach(function(q) {
                var conn = this.makeConnection();
                conn.connect();
                conn.query(q,
                    function(err, rows, fields) {
                        conn.end();
                        if (typeof rows !== 'undefined') {
                            logger.info(rows[0].affectedRows + ' row(s) affected.');
                            cb();
                        }
                        if (err) {
                            cb(err);
                        }

                    });
            });
        }
    }



};
