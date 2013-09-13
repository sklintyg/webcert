'use strict';

/* Controllers */
angular.module('wcAdminApp').controller('WebCertAdminCtrl', [ '$scope', '$window', function WebCertCtrl($scope, $window) {
    // Main controller
} ]);


/*
 *  ListUnsignedCertCtrl - Controller for logic related to displaying the list of unsigned certificates 
 * 
 */
angular.module('wcAdminApp').controller('ListQACtrl', [ '$scope', 'adminViewService', '$log', '$timeout', function ListQACtrl($scope, adminViewService, $log, $timeout) {
    $log.debug("ListUnsignedCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

   
} ]);

