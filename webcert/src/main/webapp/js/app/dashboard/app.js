'use strict';

/* Lakare Dashboard App Module */
angular.module('wcDashBoardApp', ['ui.bootstrap', 'modules.messages', 'wc.common.directives']);
angular.module('wcDashBoardApp').config([ '$routeProvider', function ($routeProvider) {
    $routeProvider.when('/index', {
	        templateUrl: '/views/dashboard/index.html',
	        controller: 'WebcertCtrl'
	    }).otherwise({
            redirectTo: '/index'
        });
} ]);

//Inject language resources
angular.module('wcDashBoardApp').run(['$rootScope', 'messageService', function ($rootScope, messageService) {
    $rootScope.lang = 'sv';
    $rootScope.DEFAULT_LANG = 'sv';
    messageService.addResources(webcertResources);
    messageService.addResources(commonMessageResources);
    

}]);
