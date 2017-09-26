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

/* globals logger, Promise */


'use strict';
var mysql = require('mysql');

/*
USAGE:
        dbPool.getConnection().then(function(connection) {
            connection.query(query,
                function(err, rows, fields) {
                    connection.release();
                    logger.info('MySQL Connection is released back into the pool');
                    if (err) {
                        reject(err);
                    } else {
                        //Do something with result
                        resolve(rows);
                    }
                });
        });
		
		
*/

logger.info('#### Skapar ny Mysql Pool för ' + process.env.SM_DATABASE_HOST + ' ####');
var connectionPool = mysql.createPool({
    host: process.env.SM_DATABASE_HOST,
    user: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    // database: process.env.DATABASE_NAME,
    multipleStatements: true,
    connectionLimit: 5
});


module.exports = {

    getConnection: function() {

        return new Promise(function(resolve, reject) {
            //logger.debug(connectionPool);

            connectionPool.getConnection(function(err, connection) {
                if (connection) {

                    //logger.debug(connectionPool);
                    if (connectionPool._allConnections.length > 1) {
                        logger.info('Number of active MySQL connections:', connectionPool._allConnections.length.toString());
                        //logger.debug(connectionPool._allConnections);
                        logger.info('Connection Queue: ', connectionPool._connectionQueue.length.toString());
                        //logger.debug(connectionPool._connectionQueue);
                        logger.info('Available Connections', connectionPool._freeConnections.length.toString());
                        //logger.debug(connectionPool._freeConnections);
                    }

                    //return connection to the module who called this function.
                    resolve(connection);

                } else {
                    logger.warn('Anslutningsproblem i databaskoppling');
                    logger.error('connectionPool.getConnection failed');
                    reject(err);
                }
            });

        }).catch(function(reason) {
            throw (reason.message);
        });
    }
};
