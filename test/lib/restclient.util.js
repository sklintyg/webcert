'use strict';

var request = require('request');

// cookie jar
var jar = request.jar();
var request = request.defaults({
    jar: jar
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