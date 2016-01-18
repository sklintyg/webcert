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

/* globals protractor, browser */
'use strict';

var request = require('request');

// cookie jar
var jar = request.jar();
var request = request.defaults({
    jar: jar,
    strictSSL: false
});

function post(options, baseUrl) {
    var defer = protractor.promise.defer();
    if(!baseUrl) {
        baseUrl = browser.baseUrl;
    }
    options.url = baseUrl + options.url;
    console.log(options.method, options.url);
    request(options, function(error, message) {
        if (error || message.statusCode >= 400) {
            console.log('Request error:', error);
            if(message) {
                console.log('Error message:', message.statusCode, message.statusMessage/*, body*/);
            }
            defer.reject({
                error: error,
                message: message
            });
        } else {
            console.log('Request success!', message.statusCode, message.statusMessage);
            defer.fulfill(message);
        }
    });
    return defer.promise;
}

function _run(options, json, baseUrl) {
    options.json = json ? json === 'json' : true;

    if(options.json) {
        options.headers = {
            'content-type': 'application/json'
        };
    } else {
        var postData = options.body;
        options.headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': Buffer.byteLength(postData)
        };
    }

    return browser.controlFlow().execute(function() {
        return post(options, baseUrl);
    });
}

module.exports = {
    run: _run
};
