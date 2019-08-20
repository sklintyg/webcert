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

/*global Promise, logger*/
'use strict';

//var htmlToText = require('html-to-text');
var Imap = require('imap');
var MailParser = require('mailparser').MailParser;

var imap = new Imap({
  user: 'intyg.test@gmail.com',
  password: process.env.DATABASE_PASSWORD,
  host: 'imap.gmail.com',
  port: 993,
  tls: true
});

function openInbox(cb) {
  imap.openBox('INBOX', true, cb);
}

function parseMail(buffer) {
  return new Promise(function(resolve, reject) {
    var mailparser = new MailParser();
    mailparser.on('end', function(mailObject) {
      //logger.silly(mailObject);
      resolve(mailObject.html);
    });
    mailparser.write(buffer);
    mailparser.end();
  });
}

module.exports = {
  readRecentMails: function() {
    return new Promise(function(resolve, reject) {
      imap.once('ready', function() {
        openInbox(function(err, box) {
          if (err) {
            throw err;
          }
          // var bufferCache = '';
          var now = new Date();
          var date5MinAgo = new Date(now.getTime() - 5 * 60000);
          logger.silly(date5MinAgo);
          var mailArray = [];
          imap.search(['UNSEEN', ['SINCE', date5MinAgo.toISOString()]],
              function(err, results) {
                if (err) {
                  reject(err);
                  logger.silly('you are already up to date');
                }
                var f = imap.fetch(results, {
                  bodies: ''
                });
                f.on('message', function(msg, seqno) {
                  logger.silly('Message #%d', seqno);
                  var buffer = '';
                  msg.on('body', function(stream, info) {
                    //logger.silly(prefix + 'Body');
                    stream.on('data', function(chunk) {
                      buffer += chunk.toString('utf8');
                    });

                    stream.once('end', function() {
                      mailArray.push(buffer);
                    });
                  });
                  msg.once('attributes', function(attrs) {
                    //logger.silly(prefix + 'Attributes: %s', inspect(attrs, false, 8));
                  });
                  msg.once('end', function() {
                    //logger.silly(prefix + 'Finished');

                  });
                });
                f.once('error', function(err) {
                  logger.silly('Fetch error: ' + err);
                  reject(err);

                });
                f.once('end', function() {
                  logger.silly('Done fetching all messages!');
                  var promiseArr = [];
                  for (var i = 0; i < mailArray.length; i++) {
                    promiseArr.push(parseMail(mailArray[i]));
                  }
                  resolve(Promise.all(promiseArr));

                });
              }
          );

        });
      });
      imap.once('error', function(err) {
        logger.silly('imap.once error');
        reject(err);

      });

      imap.once('end', function() {
        logger.silly('imap-connection ended');
        reject('Inga poster hittade');
      });

      imap.connect();

    });
  }

};
