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

/*global Promise,logger*/
'use strict';

var Imap = require('imap');

var imap = new Imap({
    user: 'intyg.test@gmail.com',
    password: 'b4pelsin',
    host: 'imap.gmail.com',
    port: 993,
    tls: true
});

function openInbox(cb) {
    imap.openBox('INBOX', true, cb);
}

module.exports = {
    countRecentMailsWithSubjectAndBody: function(mailHeader, mailBody) {
        return new Promise(function(resolve, reject) {
            imap.once('ready', function() {
                openInbox(function(err, box) {
                    if (err) {
                        reject(err);
                    }

                    var now = new Date();
                    var date5MinAgo = new Date(now.getTime() + 5 * 60000);
                    imap.search(['UNSEEN', ['SINCE', date5MinAgo.toISOString()],
                        ['HEADER', 'SUBJECT', mailHeader],
                        ['BODY', mailBody]
                    ], function(err, results) {

                        if (err) {
                            throw err;
                        }

                        logger.info(results);
                        resolve(results.length);
                    });
                });
            });

            imap.once('error', function(err) {
                console.log('imap.once error');
                reject(err);

            });

            imap.once('end', function() {
                console.log('imap-connection ended');
                reject('Inga poster hittade');
            });

            imap.connect();
        });
    }


};
