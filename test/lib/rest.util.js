/**
 * Created by BESA on 2015-11-17.
 */
'use strict';
var restClient = require('./restclient.util.js');

module.exports = {
    login: function(userJson) {
        var options = {
            url: 'fake',
            method: 'POST',
            body: 'userJsonDisplay=' + JSON.stringify(userJson)
        };
        console.log(options.body);
        return restClient.run(options, 'urlenc');
    },
    createUtkast: function(intygTyp, createJson) {
        var options = {
            url: 'api/utkast/' + intygTyp,
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json');
    },
    deleteAllUtkast: function() {
        var options = {
            url: 'testability/intyg',
            method: 'DELETE'
        };
        return restClient.run(options, 'json');
    }
};