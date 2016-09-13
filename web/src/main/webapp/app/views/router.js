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
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    'use strict';

    var commonPath = '/web/webjars/common/webcert/';

    $stateProvider.
        state('integrationenhetsval', {
            url: '/integration-enhetsval',
            views: {
                'content@': {
                    templateUrl: '/app/views/integrationEnhetsval/enhetsval.page.html',
                    controller: 'integration.EnhetsvalPageCtrl'
                }
            }
        }).
        state('webcert', {
            views: {
                'header': {
                    templateUrl: commonPath + 'gui/headers/wcHeader.partial.html',
                    controller: 'common.wcHeaderController'
                }
            }
        }).

        state('webcert.create-index', {
            url: '/create/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
                    controller: 'webcert.InitCertCtrl'
                }
            }
        }).
        state('webcert.create-choosepatient-index', {
            url: '/create/choose-patient/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
                    controller: 'webcert.ChoosePatientCtrl'
                }
            }
        }).
        state('webcert.create-edit-patientname', {
            url:'/create/edit-patient-name/:mode',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivPatientName.html',
                    controller: 'webcert.EditPatientNameCtrl'
                }
            }
        }).
        state('webcert.create-choose-certtype-index', {
            url:'/create/choose-cert-type/index',
            views: {
                'content@': {
                    templateUrl: '/app/views/sokSkrivIntyg/sokSkrivValjUtkastType.html',
                    controller: 'webcert.ChooseCertTypeCtrl'
                }
            }
        }).
        state('webcert.unhandled-qa', {
            url:'/unhandled-qa',
            views: {
                'content@': {
                    templateUrl: '/app/views/fragorOchSvar/fragorOchSvar.html',
                    controller: 'webcert.UnhandledQACtrl'
                }
            }
        }).
        state('webcert.unsigned', {
            url: '/unsigned',
            views: {
                'content@': {
                    templateUrl: '/app/views/ejSigneradeUtkast/ejSigneradeUtkast.html',
                    controller: 'webcert.UnsignedCertCtrl'
                }
            }
        }).
        state('webcert.intyg', {
            abstract : true, // jshint ignore:line
            data: { defaultActive : 'index' },
            views: {
                'content@' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.main.html'
                }
            }
        }).
        state('webcert.intyg.fk', {
            views: {
                'main@webcert.intyg': {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.fk.html',
                    controller: 'webcert.ViewCertCtrl'
                }
            }
        }).
        state('webcert.intyg.ts', {
            views: {
                'main@webcert.intyg' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.ts.html',
                    controller: 'webcert.ViewCertCtrl'
                }
            }
        }).
        state('webcert.fragasvar', {
            views: {
                'content@' : {
                    templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html',
                    controller: 'webcert.ViewCertCtrl'
                }
            }
        }).
        state('webcert.webcert-about', {
            url: '/webcert/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.webcert.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.terms-about', {
            url: '/terms/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.terms.html',
                    controller: 'webcert.AboutWebcertTermsCtrl'
                }
            }
        }).
        state('webcert.support-about', {
            url: '/support/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.support.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.certificates-about', {
            url: '/certificates/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.certificates.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.faq-about', {
            url: '/faq/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.faq.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.cookies-about', {
            url: '/cookies/about',
            views: {
                'content@': {
                    templateUrl: '/app/views/omWebcert/omWebcert.cookies.html',
                    controller: 'webcert.AboutWebcertCtrl'
                }
            }
        }).
        state('webcert.terms', {
            url: '/terms',
            views: {
                'dialogs@webcert': {
                    templateUrl: '/app/views/terms/terms.main.html',
                    controller: 'webcert.TermsCtrl'
                }
            }
        });

        $urlRouterProvider.when('', '/create/index');

});
