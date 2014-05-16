define([ 'angular', 'app' ], function(angular, app) {
    'use strict';

    app.config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/create/index', {
                // Route to initialize the create flow, template will be ignored.
                templateUrl: '/views/dashboard/create.choose-patient.html',
                controller: 'InitCertCtrl'
            }).
            when('/create/choose-patient/index', {
                templateUrl: '/views/dashboard/create.choose-patient.html',
                controller: 'ChoosePatientCtrl'
            }).
            when('/create/edit-patient-name/index', {
                templateUrl: '/views/dashboard/create.edit-patient-name.html',
                controller: 'EditPatientNameCtrl'
            }).
            when('/create/choose-cert-type/index', {
                templateUrl: '/views/dashboard/create.choose-cert-type.html',
                controller: 'ChooseCertTypeCtrl'
            }).
            when('/unhandled-qa', {
                templateUrl: '/views/dashboard/unhandled-qa.html',
                controller: 'UnhandledQACtrl'
            }).
            when('/unsigned', {
                templateUrl: '/views/dashboard/unsigned.html',
                controller: 'UnsignedCertCtrl'
            }).
            when('/view/:certificateType/:certificateId', {
                templateUrl: '/views/dashboard/view.certificate.html',
                controller: 'ViewCertCtrl'
            }).
            when('/qa/:certificateType/:certificateId', {
                templateUrl: '/views/dashboard/view.qa.html',
                controller: 'ViewQaCtrl'
            }).
            when('/support/about', {
                templateUrl: '/views/dashboard/about.support.html',
                controller: 'AboutWebcertCtrl'
            }).
            when('/certificates/about', {
                templateUrl: '/views/dashboard/about.certificates.html',
                controller: 'AboutWebcertCtrl'
            }).
            when('/faq/about', {
                templateUrl: '/views/dashboard/about.faq.html',
                controller: 'AboutWebcertCtrl'
            }).
            when('/cookies/about', {
                templateUrl: '/views/dashboard/about.cookies.html',
                controller: 'AboutWebcertCtrl'
            }).
            otherwise({
                redirectTo: '/create/index'
            });
    }]);

    return null;
});
