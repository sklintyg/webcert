define([
    'angular',
    'controllers/AboutWebcertCtrl',
    'controllers/ChooseCertTypeCtrl',
    'controllers/ChoosePatientCtrl',
    'controllers/EditPatientNameCtrl',
    'controllers/InitCertCtrl',
    'controllers/ListUnsignedCertCtrl',
    'controllers/ReadyToSignCertCtrl',
    'controllers/UnansweredCertCtrl',
    'controllers/UnhandledQACtrl',
    'controllers/UnsignedCertCtrl',
    'controllers/UnsignedCertCtrl'
], function (angular, AboutWebcertCtrl, ChooseCertTypeCtrl, ChoosePatientCtrl, EditPatientNameCtrl, InitCertCtrl, ListUnsignedCertCtrl, ReadyToSignCertCtrl, UnansweredCertCtrl, UnhandledQACtrl, UnsignedCertCtrl, WebCertCtrl) {
    'use strict';

    var moduleName = 'wc.dashboard.controllers';

    angular.module(moduleName, [])
        .controller('AboutWebcertCtrl', AboutWebcertCtrl)
        .controller('ChooseCertTypeCtrl', ChooseCertTypeCtrl)
        .controller('ChoosePatientCtrl', ChoosePatientCtrl)
        .controller('EditPatientNameCtrl', EditPatientNameCtrl)
        .controller('InitCertCtrl', InitCertCtrl)
        .controller('ListUnsignedCertCtrl', ListUnsignedCertCtrl)
        .controller('ReadyToSignCertCtrl', ReadyToSignCertCtrl)
        .controller('UnansweredCertCtrl', UnansweredCertCtrl)
        .controller('UnhandledQACtrl', UnhandledQACtrl)
        .controller('UnsignedCertCtrl', UnsignedCertCtrl)
        .controller('WebCertCtrl', WebCertCtrl);

    return moduleName;
});
