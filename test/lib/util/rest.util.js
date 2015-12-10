/**
 * Created by BESA on 2015-11-17.
 */
/*globals envConfig*/
'use strict';
var restClient = require('./restClient.util.js');

var env = envConfig;
if(!envConfig) {
    env = process.env;
}

module.exports = {
    login: function(userJson) {

        // login with doctor Jan Nilsson if noone else is specified
        var user = userJson || {
            'fornamn': 'Jan',
            'efternamn': 'Nilsson',
            'hsaId': 'IFV1239877878-1049',
            'enhetId': 'IFV1239877878-1042',
            'lakare': true,
            'forskrivarKod': '2481632'
        };

        var options = {
            url: 'fake',
            method: 'POST',
            body: 'userJsonDisplay=' + JSON.stringify(user)
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
    deleteUtkast: function(id) {
        var options = {
            url: 'testability/intyg/' + id,
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
        return restClient.run(options, 'json', env.INTYGTJANST_URL + '/resources/');
    },
    deleteAllIntyg: function() {
        var options = {
            url: 'certificate/',
            method: 'DELETE'
        };
        return restClient.run(options, 'json', env.INTYGTJANST_URL + '/resources/');
    },
    deleteIntyg: function(id) {
        var options = {
            url: 'certificate/' + id,
            method: 'DELETE'
        };
        return restClient.run(options, 'json', env.INTYGTJANST_URL + '/resources/');
    }
};