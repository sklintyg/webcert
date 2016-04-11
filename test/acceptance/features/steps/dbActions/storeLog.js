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

/* globals Promise, wcTestTools, JSON,logger, conn */

'use strict';
var testdataHelper = wcTestTools.helpers.testdata;

function formatDate(date) {
    var time = ((date.getHours() < 10) ? '0' : '') +
        date.getHours() + ':' +
        ((date.getMinutes() < 10) ? '0' : '') +
        date.getMinutes() + ':' +
        ((date.getSeconds() < 10) ? '0' : '') + date.getSeconds();
    return testdataHelper.dateFormat(date) + 'T' + time;
}
// function fetchLogEntries(activity) {
//     var p1 = new Promise(function(resolve, reject) {
//         getLogEntries(activity, intyg.id, global.user.hsaId)
//             .then(function(result) {
//                 resolve(result);
//             }, function(reason) {
//                 reject(reason);
//             });
//     });
//     return p1;
// }

function checkLogEntries(activity, count, intygsID, userHSA) {
    var p1 = new Promise(function(resolve, reject) {
        getLogEntries(activity, intygsID, userHSA)
            .then(function(result) {
                if (result.length >= count) {
                    resolve('Hittade rader: ' + JSON.stringify(result));
                } else {
                    reject('Hittade färre än ' + count + 'rader i databasen');
                }
            }, function(reason) {
                reject(reason);
            });
    });

    return p1;
}

function getLogEntries(activity, intygsID, userHSA) {
    var dbTable = 'webcert_requests.storelog__mock_requests';
    var now = new Date();
    var oneMinuteSinceNow = new Date(now.getTime() + (-1) * 60000);
    oneMinuteSinceNow = formatDate(oneMinuteSinceNow);

    var query = 'SELECT * FROM ' + dbTable + ' where ' +
        ' activityLevel = "' + intygsID + '"' +
        ' AND activitytype = "' + activity + '"' +
        ' AND userid = "' + userHSA + '"' +
        ' AND logtime>="' + oneMinuteSinceNow + '"';

    console.log('query: ' + query);
    var p1 = new Promise(function(resolve, reject) {
        conn.query(query,
            function(err, rows, fields) {
                if (err) {
                    reject(err);
                }
                resolve(rows);
            });
    });
    return p1;
}

function waitForCount(activity, count, intygsID, userHSA, cb) {
    global.conn = require('./makeConnection')();
    conn.connect();

    checkLogEntries(activity, count, intygsID, userHSA).then(function(val) {
        logger.info('OK: ' + val);
        conn.end();
        cb();
    }, function(err) {
        console.log('FEL: ' + err);
        //console.log('Ny kontroll sker efter ' + intervall + 'ms');
        setTimeout(function() {
            waitForCount(activity, count, intygsID, userHSA, cb);
        }, 5000);
    });
}


module.exports = {
    waitForCount: waitForCount
};
