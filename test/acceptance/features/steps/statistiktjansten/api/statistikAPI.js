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

/* globals logger, protractor, browser*/
'use strict';
var request = require('request');
var helpers = require('../../helpers');

var defaultRESTAPIOptions = {
    url: process.env.STATISTIKTJANST_URL + '/api/',
    method: 'GET',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength('')
    },
    body: ''
};


module.exports = {
    processIntyg: function() {
        var options = defaultRESTAPIOptions;
        options.method = 'POST';
        options.url = process.env.STATISTIKTJANST_URL + '/api/testsupport/processIntyg';
        return apiRequest(options);
    },
    setFilter: function(body) {
        var options = defaultRESTAPIOptions;
        options.url = options.url + 'filter';
        options.method = 'POST';

        if (body) {
            options.body = body;
        } else {
            options.body = {
                'enheter': [],
                'verksamhetstyper': [],
                'sjukskrivningslangd': [],
                'aldersgrupp': [],
                'intygstyper': [],
                'diagnoser': [],
                'fromDate': null,
                'toDate': null,
                'useDefaultPeriod': true
            };
        }
        return apiRequest(options);
    },
    getMeddelandenPerAmne: function(vardgivare, filter) {
        /*var options = defaultRESTAPIOptions;
        options.url = process.env.STATISTIKTJANST_URL + '/api/' + 'verksamhet/getMeddelandenPerAmne?vgid=' + vardgivare;;

        console.log(options.url);
        console.log('https://statistik.ip30.nordicmedtest.se/api/verksamhet/getMeddelandenPerAmne?vgid=TSTNMT2321000156-107M');


        if (filter) {
            options.url += '&filter=' + filter;
        }
        return apiRequest(options);*/
        browser.ignoreSynchronization = true;
        return helpers.getUrl(process.env.STATISTIKTJANST_URL + '/api/' + 'verksamhet/getMeddelandenPerAmne?vgid=' + vardgivare);
    },
    getMeddelandenPerAmneLandsting: function() {

    },
    getMeddelandenPerAmneOchEnhetLandsting: function() {

    },
    getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet: function() {

    },
    getMeddelandenPerAmneOchEnhetVerksamhet: function() {

    },
    getMeddelandenPerAmneTvarsnittVerksamhet: function() {

    },
    getMeddelandenPerAmneVerksamhet: function() {

    },
    getNumberOfMeddelandenPerMonth: function() {

    },
    getNumberOfMeddelandenPerMonthTvarsnittVerksamhet: function() {

    },
    getNumberOfMeddelandenPerMonthVerksamhet: function() {

    }
};



function apiRequest(options) {
    var defer = protractor.promise.defer();
    logger.silly(options.url);
    logger.silly(options.body);


    request(options, function(error, message) {
        if (error || message.statusCode >= 400) {
            logger.info('Request error:', error);
            if (message) {
                logger.error('Error message: ' + message.statusCode, message.statusMessage /*, body*/ );
            }
            defer.reject({
                error: error,
                message: message
            });
        } else {
            logger.info('Request success!', message.statusCode, message.statusMessage);
            defer.fulfill(message);
        }
    });
    return defer.promise;
}


/*	Available API endpoints
api.clearLandstingEnhets
api.getAgeGroups
api.getAgeGroupsTimeSeriesVerksamhet
api.getAgeGroupsVerksamhet
api.getBusinessOverview
api.getCompareDiagnosisTimeSeriesVerksamhet
api.getCompareDiagnosisVerksamhet
api.getDegreeOfSickLeave
api.getDegreeOfSickLeaveTvarsnittVerksamhet
api.getDegreeOfSickLeaveVerksamhet
api.getDiagnosisGroupData
api.getDiagnosisGroupDataVerksamhet
api.getDiagnosisGroupTvarsnittVerksamhet
api.getDiagnosisKapitelAndAvsnittAndKategori
api.getFilterData
api.getFilterHash
api.getIcd10Structure
api.getIntygPerTypePerMonthLandsting
api.getIntygPerTypePerMonthNationell
api.getIntygPerTypePerMonthVerksamhet
api.getIntygPerTypeTvarsnittVerksamhet
api.getLandstingFilterInfo
api.getLastLandstingUpdateInfo
api.getLoginInfo
api.getLongSickLeavesDataVerksamhet
api.getLongSickLeavesTvarsnittVerksamhet
api.getMeddelandenPerAmne
api.getMeddelandenPerAmneLandsting
api.getMeddelandenPerAmneOchEnhetLandsting
api.getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet
api.getMeddelandenPerAmneOchEnhetVerksamhet
api.getMeddelandenPerAmneTvarsnittVerksamhet
api.getMeddelandenPerAmneVerksamhet
api.getNationalCountyData
api.getNationalSickLeaveLengthData
api.getNationalSjukfallPerSexData
api.getNumberOfCasesPerMonth
api.getNumberOfCasesPerMonthLandsting
api.getNumberOfCasesPerMonthTvarsnittVerksamhet
api.getNumberOfCasesPerMonthVerksamhet
api.getNumberOfIntygPerMonthTvarsnittVerksamhet
api.getNumberOfIntygPerMonthVerksamhet
api.getNumberOfMeddelandenPerMonth
api.getNumberOfMeddelandenPerMonthTvarsnittVerksamhet 
api.getNumberOfMeddelandenPerMonthVerksamhet 
api.getOverview
api.getSickLeaveLengthDataVerksamhet
api.getSickLeaveLengthTimeSeriesDataVerksamhet
api.getSjukfallPerBusinessLandsting
api.getSjukfallPerBusinessTimeSeriesVerksamhet
api.getSjukfallPerBusinessVerksamhet
api.getSjukfallPerLakarbefattningTidsserieVerksamhet
api.getSjukfallPerLakarbefattningVerksamhet
api.getSjukfallPerLakaresAlderOchKonTidsserieVerksamhet
api.getSjukfallPerLakaresAlderOchKonVerksamhet
api.getSjukfallPerLakareSomTidsserieVerksamhet
api.getSjukfallPerLakareVerksamhet
api.getSjukfallPerPatientsPerBusinessLandsting
api.getStaticFilterData
api.getSubDiagnosisGroupData
api.getSubDiagnosisGroupDataVerksamhet
api.getSubDiagnosisGroupTvarsnittVerksamhet
api.getUserAccessInfo
api.logOnServer
*/
