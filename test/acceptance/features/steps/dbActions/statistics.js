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

/* globals Promise,JSON,logger */

'use strict';
var connection;

function getIntygEntries(intygsID) {
    var bdName = process.env.STAT_DATABASE_NAME;
    var dbTable = bdName + '.intyghandelse';
    var column = 'correlationId';
    var query = 'SELECT ' + column + ' FROM ' + dbTable + ' WHERE ' + column + ' = "' + intygsID + '"';

    console.log('query: ' + query);
    var p1 = new Promise(function(resolve, reject) {
        connection.query(query,
            function(err, rows, fields) {
                if (err) {
                    reject(err);
                }
                resolve(rows);
            });
    });
    return p1;
}

/*function deleteEntries(diagnosKod) {
    var bdName = process.env.STAT_DATABASE_NAME;
    var dbTable = bdName + '.wideline';
    var column = 'diagnoskategori';
    var query = 'DELETE FROM ' + dbTable + ' WHERE ' + column + ' = "' + diagnosKod + '"';

    console.log('query: ' + query);
    var p1 = new Promise(function(resolve, reject) {
        connection.query(query,
            function(err, rows, fields) {
                if (err) {
                    reject(err);
                }
                resolve(rows);
            });
    });
    return p1;
}*/

function lookUp(count, intygsID, cb) {
    handleDisconnect();

    var intervall = 5000;

    getIntygEntries(intygsID).then(function(result) {
        if (result.length >= count) {
            logger.info('Hittade rader: ' + JSON.stringify(result));
            connection.end();
            cb();
        } else {
            logger.info('Hittade färre än ' + count + 'rader i databasen');
            console.log('Ny kontroll sker efter ' + intervall + 'ms');
            setTimeout(function() {
                lookUp(count, intygsID, cb);
            }, intervall);
        }

    }, function(err) {
        connection.end();
        cb(err);

    });
}

function deleteSjukfall(diagnosKod, cb) {
    handleDisconnect();

    var intervall = 5000;

    getIntygEntries(diagnosKod).then(function(result) {
        if (result.length >= 0) {
            logger.info(result.length + 'rader togs bort: ' + JSON.stringify(result));
            connection.end();
            cb();
        } else {
            logger.info('Hittade färre än 1 rader i databasen');
            console.log('Ny kontroll sker efter ' + intervall + 'ms');
            setTimeout(function() {
                lookUp(diagnosKod, cb);
            }, intervall);
        }

    }, function(err) {
        connection.end();
        cb(err);

    });
}

function handleDisconnect() {
    connection = require('./makeConnectionStatistics')();

    connection.connect(function(err) {
        if (err) {
            logger.warn('Fel i anslutning till databasen:', err);
        }
    });
    connection.on('error', function(err) {
        logger.warn('Anslutningsproblem i databaskoppling' + JSON.stringify(err));
    });
}



module.exports = {
    lookUp: lookUp,
    deleteSjukfall: deleteSjukfall
};
