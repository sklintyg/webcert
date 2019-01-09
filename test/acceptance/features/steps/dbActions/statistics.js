/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
var statsPool = require('./pool');

function getIntygEntries(intygsID, connection) {
    var dbName = process.env.STAT_DATABASE_NAME;
    var dbTable = dbName + '.intyghandelse';
    var column = 'correlationId';
    var query = 'SELECT ' + column + ' FROM ' + dbTable + ' WHERE ' + column + ' = "' + intygsID + '"';

    logger.silly('query: ' + query);
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

function lookUp(count, intygsID, cb) {


    statsPool.getConnection().then(function(connection) {

        var intervall = 5000;

        getIntygEntries(intygsID, connection).then(function(result) {
            if (result.length >= count) {
                logger.info('Hittade rader: ' + JSON.stringify(result));
                connection.release();
                cb();
            } else {
                connection.release();
                logger.info('Hittade färre än ' + count + 'rader i databasen');
                logger.silly('Ny kontroll sker efter ' + intervall + 'ms');
                setTimeout(function() {
                    lookUp(count, intygsID, cb);
                }, intervall);
            }

        }, function(err) {
            connection.release();
            cb(err);

        });
    });
}



module.exports = {
    lookUp: lookUp
};
