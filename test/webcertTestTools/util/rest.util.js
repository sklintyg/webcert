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

/**
 * Created by BESA on 2015-11-17.
 */
/*globals JSON*/
'use strict';
var restClient = require('./restclient.util.js');
var env = require('./../environment.js').envConfig;

module.exports = {

    login: function(userJson) {

        // login with doctor Leonie Koehl if noone else is specified
        var user = userJson || {
                'hsaId': 'TSTNMT2321000156-103F',
                'forNamn': 'Leonie',
                'efterNamn': 'Koehl',
                'enhetId': 'TSTNMT2321000156-1039',
                'legitimeradeYrkesgrupper': [ 'Läkare' ],
                'forskrivarKod': '9300005',
                'befattningsKod': '203090',
                'origin': 'NORMAL'
            };

        var options = {
            url: 'fake',
            method: 'POST',
            body: 'userJsonDisplay=' + JSON.stringify(user)
        };
        return restClient.run(options, 'urlenc');
    },

    // Utkast/intyg

    createUtkast: function(intygTyp, createJson) {
        var options = {
            url: 'api/utkast/' + intygTyp,
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json');
    },
    saveUtkast: function(intygsTyp, intygsId, version, utkastJson) {
        var options = {
            url: 'moduleapi/utkast/' + intygsTyp + '/' + intygsId + '/' + version,
            method: 'PUT',
            body: utkastJson
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
    createWebcertIntyg: function(createJson) {
        var options = {
            url: 'testability/intyg/utkast',
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json');
    },

    // Ärenden

    createArende: function(createJson) {
        var options = {
            url: 'testability/arendetest/',
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json');
    },
    deleteAllArenden: function() {
        var options = {
            url: 'testability/arendetest/',
            method: 'DELETE'
        };
        return restClient.run(options, 'json');
    },
    deleteArende: function(id) {
        var options = {
            url: 'testability/arendetest/' + id,
            method: 'DELETE'
        };
        return restClient.run(options, 'json');
    },

    // Fråga/svar
    createFragasvar: function(createJson) {
        var options = {
            url: 'testability/fragasvar/',
            method: 'POST',
            body: createJson
        };
        return restClient.run(options, 'json');
    },
    deleteAllFragasvar: function() {
        var options = {
            url: 'testability/fragasvar/',
            method: 'DELETE'
        };
        return restClient.run(options, 'json');
    },

    // Intygstjänst - intyg

    createIntyg: function(createJson) {
        var options = {
            url: 'certificate/',
            method: 'POST',
            body: createJson
        };
        //logger.debug("================================================CREATEJSON");
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
    },
    getIntyg: function(id) {
        var options = {
            url: 'certificate/' + id,
            method: 'GET'
        };
        return restClient.run(options, 'json', env.INTYGTJANST_URL + '/resources/');
    },
    queryNotificationStub: function() {
        var options = {
            url: 'services/notification-stub/notifieringar/v3',
            method: 'GET'
        };
        return restClient.run(options, 'json');
    },
    registerEnhetAsDjupintegrerad: function(veId, veNamn, vgId, vgNamn, v1Enabled, v2Enabled) {
        var options = {
            url: 'testability/integreradevardenheter/',
            method: 'POST',
            body: {
                enhetsId: veId,
                enhetsNamn: veNamn,
                vardgivareId: vgId,
                vardgivareNamn: vgNamn,
                schemaVersion: v2Enabled ? "2.0" : "1.0"
            }
        };
        return restClient.run(options, 'json');
    },
    deregisterEnhetAsDjupintegrerad: function(veId) {
        var options = {
            url: 'testability/integreradevardenheter/' + veId,
            method: 'DELETE'
        };
        return restClient.run(options, 'json');
    }
};
