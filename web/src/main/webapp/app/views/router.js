/**
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider) {
    'use strict';
    $stateProvider.

        state('create-index', {
            url: '/create/index',
            templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
            controller: 'webcert.InitCertCtrl'
        }).
        state('create-choosepatient-index', {
            url: '/create/choose-patient/index',
            templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
            controller: 'webcert.ChoosePatientCtrl'
        }).
        state('create-edit-patientname', {
            url:'/create/edit-patient-name/:mode',
            templateUrl: '/app/views/sokSkrivIntyg/sokSkrivPatientName.html',
            controller: 'webcert.EditPatientNameCtrl'
        }).
        state('create-choose-certtype-index', {
            url:'/create/choose-cert-type/index',
            templateUrl: '/app/views/sokSkrivIntyg/sokSkrivValjUtkastType.html',
            controller: 'webcert.ChooseCertTypeCtrl'
        }).
        state('unhandled-qa', {
            url:'/unhandled-qa',
            templateUrl: '/app/views/fragorOchSvar/fragorOchSvar.html',
            controller: 'webcert.UnhandledQACtrl'
        }).
        state('unsigned', {
            url: '/unsigned',
            templateUrl: '/app/views/ejSigneradeUtkast/ejSigneradeUtkast.html',
            controller: 'webcert.UnsignedCertCtrl'
        }).
        state('intyg', {
            url:'/intyg/:certificateType/:certificateId?:patientId&:hospName&:signed',
            views: {
                '' : {
                    templateUrl: '/app/views/visaIntygFragasvar/intyg.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'header@intyg' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('fragasvar', {
            url: '/fragasvar/:certificateType/:certificateId',
            views: {
                '' : {
                    templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'header@fragasvar' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('fragasvar-qaonly', {
            url: '/fragasvar/:certificateType/:certificateId/:qaOnly',
            views: {
                '' : {
                    templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html',
                    controller: 'webcert.ViewCertCtrl'
                },
                'header@fragasvar-qaonly' : {
                    templateUrl: '/web/webjars/common/webcert/intyg/intyg-header/intyg-header.html',
                    controller: 'common.IntygHeader'
                }
            }
        }).
        state('webcert-about', {
            url: '/webcert/about',
            templateUrl: '/app/views/omWebcert/omWebcert.webcert.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('support-about', {
            url: '/support/about',
            templateUrl: '/app/views/omWebcert/omWebcert.support.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('certificates-about', {
            url: '/certificates/about',
            templateUrl: '/app/views/omWebcert/omWebcert.certificates.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('faq-about', {
            url: '/faq/about',
            templateUrl: '/app/views/omWebcert/omWebcert.faq.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('cookies-about', {
            url: '/cookies/about',
            templateUrl: '/app/views/omWebcert/omWebcert.cookies.html',
            controller: 'webcert.AboutWebcertCtrl'
        });

        $urlRouterProvider.when('', '/create/index');
});