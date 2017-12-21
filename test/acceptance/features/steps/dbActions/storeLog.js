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

/* globals Promise, wcTestTools, JSON,logger */

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;
var dbPool = require('./smPool');


function formatDate(date) {
    var time = ((date.getHours() < 10) ? '0' : '') +
        date.getHours() + ':' +
        ((date.getMinutes() < 10) ? '0' : '') +
        date.getMinutes() + ':' +
        ((date.getSeconds() < 10) ? '0' : '') + date.getSeconds();
    return testdataHelper.dateFormat(date) + 'T' + time;
}

function getLogEntries(activity, intygsID, userHSA, connection, activityArg) {
    var dbTable = 'webcert_requests.storelog__mock_requests';
    var now = new Date();
    var oneMinuteSinceNow = new Date(now.getTime() + (-2) * 60000);
    oneMinuteSinceNow = formatDate(oneMinuteSinceNow);

    var query = `SELECT * FROM ${dbTable} WHERE
        activityLevel = "${intygsID}"
        AND activitytype = "${activity}"
        AND userid = "${userHSA}"
        AND logtime>="${oneMinuteSinceNow}"`;

    if (activityArg) {
        query += ` AND activityarg = "${activityArg}"`;
    }

    console.log('query: ' + query);
    var p1 = new Promise(function(resolve, reject) {

        connection.query(query,
            function(err, rows, fields) {
                if (err) {
                    reject(err, connection);
                }
                resolve(rows, connection);
            });

    });
    return p1;
}

function waitForCount(activity, count, intygsID, userHSA, activityArg, counter) {
    return new Promise(function(resolve, reject) {
        if (!counter) {
            counter = 0;
        }

        return dbPool.getConnection()
            .then(connection => getLogEntries(activity, intygsID, userHSA, connection, activityArg)
                .then(result => {
                    var interval = 5000;
                    if (result.length >= count) {
                        logger.info('Hittade rader: ' + JSON.stringify(result));
                        connection.release();
                        resolve();
                        return result;
                    } else {
                        logger.info(`Hittade färre än ${count} rader i databasen`);
                        if (counter >= 9) {
                            counter++;
                            return reject(new Error('Hittade inte ' + activity + ', ' + activityArg + '. Databas Query stoppades efter ' + counter + ' försök')).then(function(error) {
                                // not called
                            }, function(error) {
                                return console.trace(error); // Stacktrace
                            });
                        } else {
                            counter++;
                            logger.info(`Ny kontroll sker efter ${interval} ms`);
                            connection.release();
                            return setTimeout(function() {
                                return waitForCount(activity, count, intygsID, userHSA, activityArg, counter).then(function() {
                                    resolve();
                                    return;
                                }, function(err) {
                                    reject(err);
                                    return;
                                });
                            }, interval);
                        }
                    }
                }).then(function(fulfilled) {
                    return logger.silly('promise fulfilled');
                }, function(rejected) {
                    return logger.silly('promise rejected');
                }));
    }).then(function() {
        return;
    }, function(err) {
        throw (err);
    });

}

module.exports = {
    waitForCount: waitForCount
};
