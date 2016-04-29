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
 * Created by BESA on 2015-11-25.
 */
'use strict';

var restUtil = require('./../util/rest.util.js');
var intygTemplates = require('./../testdata/intygTemplates.js');
var intygGenerator = require('./../util/intygGenerator.util.js');

module.exports = {
    createIntygFromTemplate: function(intygTemplateName, intygId) {
        restUtil.login();
        var template = intygTemplates[intygTemplateName];
        template.intygId = intygId;
        var intyg = intygGenerator.buildIntyg(template);
        return restUtil.createIntyg(intyg);
    },
    deleteAllIntyg: function() {
        restUtil.login();
        return restUtil.deleteAllIntyg();
    },
    deleteIntyg: function(id) {
        restUtil.login();
        return restUtil.deleteIntyg(id);
    },
    createUtkast: function(intygType, template) {
        restUtil.login();

        var utkastTemplate = template;
        if (typeof template === 'undefined') {
            utkastTemplate = {
                'intygType': intygType,
                'patientPersonnummer': '19121212-1212',
                'patientFornamn': 'Tolvan',
                'patientEfternamn': 'Tolvansson',
                'patientPostadress': 'Svensson, Storgatan 1, PL 1234',
                'patientPostnummer': '12345',
                'patientPostort': 'Småmåla'
            };
        }
        return restUtil.createUtkast(intygType, utkastTemplate);
    },
    deleteAllUtkast: function() {
        restUtil.login();
        return restUtil.deleteAllUtkast();
    },
    deleteUtkast: function(id) {
        restUtil.login();
        return restUtil.deleteUtkast(id);
    },
    deleteAllArenden: function() {
        restUtil.login();
        return restUtil.deleteAllUtkast();
    },
    deleteArende: function(id) {
        restUtil.login();
        return restUtil.deleteUtkast(id);
    }
};
