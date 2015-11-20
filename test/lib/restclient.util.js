'use strict';

var request = require('request');

// cookie jar
var jar = request.jar();
var request = request.defaults({
    jar: jar
});

function post(options) {
    var defer = protractor.promise.defer();
    options.url = browser.baseUrl + options.url;
    console.log(options.method, options.url);
    request(options, function(error, message) {
        if (error || message.statusCode >= 400) {
            console.log('Request error:', error, message.statusCode, message.statusMessage/*, body*/);
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

function _run(options, json) {
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
        return post(options);
    });
}

module.exports = {
    run: _run
};