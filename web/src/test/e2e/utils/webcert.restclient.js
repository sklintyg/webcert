var request = require('request');

var WebCertRestClient = function() {
    'use strict';

    // cookie jar
    var jar = request.jar();
    var req = request.defaults({
        jar: jar
    });

    function post(options) {
        var defer = protractor.promise.defer();
        console.log("Calling", url);
        req(options, function(error, message) {
            console.log("Done call to", options.url);
            if (error || message.statusCode >= 400) {
                defer.reject({
                    error: error,
                    message: message
                });
            } else {
                defer.fulfill(message);
            }
        });
        return defer.promise;
    }

    this.createUtkast = function(intygTyp, json) {
        var options = {
            url: 'api/utkast/' + intygTyp,
            method: 'POST',
            json: true,
            headers: {
                'content-type': 'application/json'
            },
            body: json
        };

        return post(options);
    };

    var flow = protractor.promise.controlFlow();
    flow.execute(this.createUtkast);

}

module.export = WebCertRestClient;