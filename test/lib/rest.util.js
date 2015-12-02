/**
 * Created by BESA on 2015-11-17.
 */
'use strict';
var restClient = require('./restClient.util.js');
var envConfig = require('./envConfig.json');

module.exports = {
    login: function(userJson) {
        var options = {
            url: 'fake',
            method: 'POST',
            body: 'userJsonDisplay=' + JSON.stringify(userJson)
        };
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
    },
    createIntyg: function(createJson) {
        var options = {
            url: 'certificate/',
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json', envConfig.intygstjanstBaseurl);
    },
    deleteAllIntyg: function() {
        var options = {
            url: 'certificate/',
            method: 'DELETE'
        };
        return restClient.run(options, 'json', envConfig.intygstjanstBaseurl);
    },
    deleteIntyg: function(id) {
        var options = {
            url: 'certificate/' + id,
            method: 'DELETE'
        };
        return restClient.run(options, 'json', envConfig.intygstjanstBaseurl);
    }
};