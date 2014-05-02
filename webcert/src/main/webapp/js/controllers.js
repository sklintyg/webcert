define([
    'angular',
    'controllers/AboutWebcertCtrl',
    'controllers/ChooseCertTypeCtrl',
    'controllers/ChoosePatientCtrl',
    'controllers/EditPatientNameCtrl',
    'controllers/InitCertCtrl',
    'controllers/UnhandledQACtrl',
    'controllers/UnsignedCertCtrl',
    'controllers/UnsignedCertCtrl'
], function(angular, AboutWebcertCtrl, ChooseCertTypeCtrl, ChoosePatientCtrl, EditPatientNameCtrl, InitCertCtrl,
    UnhandledQACtrl, UnsignedCertCtrl, WebCertCtrl) {
    'use strict';

    var moduleName = 'wc.dashboard.controllers';

    angular.module(moduleName, []).
        controller('AboutWebcertCtrl', AboutWebcertCtrl).
        controller('ChooseCertTypeCtrl', ChooseCertTypeCtrl).
        controller('ChoosePatientCtrl', ChoosePatientCtrl).
        controller('EditPatientNameCtrl', EditPatientNameCtrl).
        controller('InitCertCtrl', InitCertCtrl).
        controller('UnhandledQACtrl', UnhandledQACtrl).
        controller('UnsignedCertCtrl', UnsignedCertCtrl).
        controller('WebCertCtrl', WebCertCtrl);

    return moduleName;
});
