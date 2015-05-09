/**
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider) {

    $stateProvider.

        state('create-index', {
            url: '/create/index',
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.InitCertCtrl'
        }).
        state('create-choosepatient-index', {
            url: '/create/choose-patient/index',
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.ChoosePatientCtrl'
        }).
        state('create-edit-patientname', {
            url:'/create/edit-patient-name/:mode',
            templateUrl: '/views/dashboard/create.edit-patient-name.html',
            controller: 'webcert.EditPatientNameCtrl'
        }).
        state('create-choose-certtype-index', {
            url:'/create/choose-cert-type/index',
            templateUrl: '/views/dashboard/create.choose-cert-type.html',
            controller: 'webcert.ChooseCertTypeCtrl'
        }).
        state('unhandled-qa', {
            url:'/unhandled-qa',
            templateUrl: '/views/dashboard/unhandled-qa.html',
            controller: 'webcert.UnhandledQACtrl'
        }).
        state('unsigned', {
            url: '/unsigned',
            templateUrl: '/views/dashboard/unsigned.html',
            controller: 'webcert.UnsignedCertCtrl'
        }).
        state('intyg', {
            url:'/intyg/:certificateType/:certificateId?:patientId&:hospName&:signed',
            views: {
                '' : {
                    templateUrl: '/views/dashboard/view.certificate.html',
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
                    templateUrl: '/views/dashboard/view.qa.html',
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
                    templateUrl: '/views/dashboard/view.qa.html',
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
            templateUrl: '/views/dashboard/about.webcert.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('support-about', {
            url: '/support/about',
            templateUrl: '/views/dashboard/about.support.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('certificates-about', {
            url: '/certificates/about',
            templateUrl: '/views/dashboard/about.certificates.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('faq-about', {
            url: '/faq/about',
            templateUrl: '/views/dashboard/about.faq.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('cookies-about', {
            url: '/cookies/about',
            templateUrl: '/views/dashboard/about.cookies.html',
            controller: 'webcert.AboutWebcertCtrl'
        });

        $urlRouterProvider.when('', '/create/index')


});