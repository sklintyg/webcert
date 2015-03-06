/**
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider) {

    $stateProvider.

        state('createIndex', {
            url: '/create/index',
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.InitCertCtrl'
        }).
        state('createChoosepatientIndex', {
            url: '/create/choose-patient/index',
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.ChoosePatientCtrl'
        }).
        state('createEditpatientname', {
            url:'/create/edit-patient-name/:mode',
            templateUrl: '/views/dashboard/create.edit-patient-name.html',
            controller: 'webcert.EditPatientNameCtrl'
        }).
        state('createChoosecerttypeIndex', {
            url:'/create/choose-cert-type/index',
            templateUrl: '/views/dashboard/create.choose-cert-type.html',
            controller: 'webcert.ChooseCertTypeCtrl'
        }).
        state('unhandledQa', {
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
            url:'/intyg/:certificateType/:certificateId',
            templateUrl: '/views/dashboard/view.certificate.html',
            controller: 'webcert.ViewCertCtrl'
        }).
        state('fragasvar', {
            url: '/fragasvar/:certificateType/:certificateId/:qaOnly?',
            templateUrl: '/views/dashboard/view.qa.html',
            controller: 'webcert.ViewCertCtrl'
        }).
        state('webcertAbout', {
            url: '/webcert/about',
            templateUrl: '/views/dashboard/about.webcert.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('supportAbout', {
            url: '/support/about',
            templateUrl: '/views/dashboard/about.support.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('certificatesAbout', {
            url: '/certificates/about',
            templateUrl: '/views/dashboard/about.certificates.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('faqAbout', {
            url: '/faq/about',
            templateUrl: '/views/dashboard/about.faq.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        state('cookies_about', {
            url: '/cookies/about',
            templateUrl: '/views/dashboard/about.cookies.html',
            controller: 'webcert.AboutWebcertCtrl'
        });

        $urlRouterProvider.when('', '/create/index')


});