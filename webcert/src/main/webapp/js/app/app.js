'use strict';

/* App Module */

var webcertApp = angular.module('WebcertApp', ['modules.messages']).config([ '$routeProvider', function ($routeProvider) {
    $routeProvider.when('/index', {
	        templateUrl: '/views/index.html',
	        controller: 'WebcertCtrl'
	    }).otherwise({
            redirectTo: '/index'
        });
} ]);

webcertApp.run(['$rootScope', 'messageService', function ($rootScope, messageService) {
    $rootScope.lang = 'sv';
    $rootScope.DEFAULT_LANG = 'sv';
    messageService.addResources(webcertResources);

}]);
