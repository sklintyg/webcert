define([
    'angular',
    'controllers/AboutWebcertCtrl',
    'controllers/ChooseCertTypeCtrl',
    'controllers/ChoosePatientCtrl',
    'controllers/EditPatientNameCtrl',
    'controllers/InitCertCtrl',
    'controllers/UnhandledQACtrl',
    'controllers/UnsignedCertCtrl',
    'controllers/ViewCertCtrl',
    'controllers/ViewQaCtrl'
], function(angular, AboutWebcertCtrl, ChooseCertTypeCtrl, ChoosePatientCtrl, EditPatientNameCtrl, InitCertCtrl,
    UnhandledQACtrl, UnsignedCertCtrl, ViewCertCtrl, ViewQaCtrl) {
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
        controller('ViewCertCtrl', ViewCertCtrl).
        controller('ViewQaCtrl', ViewQaCtrl);

    return moduleName;
});
