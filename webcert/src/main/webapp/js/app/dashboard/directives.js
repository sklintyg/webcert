'use strict';

/* Directives */
angular.module('wcDashBoardApp').directive("wcCareUnitClinicSelector", ['$rootScope', function ($rootScope) {
    return {
        restrict: "A",
        transclude: false,
        replace: true,
        template: 
    	    '<table class="span12 table unit-table">'+
        		'<tr ng-repeat="unit in units">'+
        			'<td><button type="button" ng-click="selectUnit(unit)" ng-href="#" class="qa-unit" ng-class="{selected : selectedUnit == unit}">{{unit.namn}}</button></td>'+
        			'<td><span class="qa-circle" ng-class="{\'qa-circle-active\': getItemCountForUnitId(unit)>0}" title="Ohanterade frågor och svar">{{getItemCountForUnitId(unit)}}</span></td>'+
        		'</tr>'+
        	'</table>',
        controller: function ($scope) {
            // init
            $scope.vardenheter = $rootScope.MODULE_CONFIG.USERCONTEXT.vardgivare.vardenheter;

            $scope.units = []; // aggregated units to present for vardenhet/mottagning choice
            
            angular.forEach($scope.vardenheter, function(vardenhet, key){
              this.push(vardenhet);
              
              angular.forEach(vardenhet.mottagningar, function(mottagning, key){
              	mottagning.namn = vardenhet.namn + ' - ' + mottagning.namn;
              	this.push(mottagning);
              }, $scope.units);
              
            }, $scope.units);
            
            $scope.selectedUnit = {};

            $scope.selectUnit = function(unit) {
            	$scope.selectedUnit = unit;
            	//call method actually on parent scope: NOTE: not very nice coupling between this directive and controller
            	$scope.setActiveUnit($scope.selectedUnit);
            }
            
            //initial selection
            $scope.selectUnit(selectFirstUnit($scope.units));
            
            // Local function getting the first care unit's hsa id in the data struct.
            function selectFirstUnit(units) {
                if (typeof units === "undefined" || units.length == 0) {
                    return null;
                } else {
                    return units[0];
                }
            }
        }
    };
}]);

angular.module('wcDashBoardApp').directive("wcAbout", ['$rootScope','$location', function($rootScope,$location) {
  return {
      restrict : "A",
      transclude : true,
      replace : true,
      scope : {
        menuDefsAbout: "@"
      },
      controller: function($scope, $element, $attrs) {
        //Expose "now" as a model property for the template to render as todays date
        $scope.today = new Date();
        $scope.menuItems = [
	        {
	        	link :'/web/dashboard#/about.support', 
	          label:'Support',
	        },
	        {
	        	link :'/web/dashboard#/about.webcert', 
	          label:'Om webcert',
	        },
        ];
        
        var currentRoute = $location.path().substring( $location.path().lastIndexOf('.') + 1) || 'index';
        $scope.currentSubMenuLabel = "";

        // set header label based on menu items label
        angular.forEach($scope.menuItems, function(menu, key) {
        	var page = menu.link.substr(menu.link.lastIndexOf('.') + 1);
        	if(page == currentRoute) {
        		$scope.currentSubMenuLabel = menu.label; 
        	}
        });
        
        $scope.isActive = function (page) {
        	page = page.substr(page.lastIndexOf('.') + 1);
          return page === currentRoute;
        };                 
      },
      template:
        '<div>'+
	        '<h1><span message key="dashboard.about.title"></span></h1>'+
	        '<div class="row-fluid">'+
	    			'<div class="span3">'+
							'<ul class="nav nav-tabs nav-stacked">'+
								'<li ng-class="{active: isActive(menu.link)}" ng-repeat="menu in menuItems">'+
									'<a ng-href="{{menu.link}}">{{menu.label}}<i class="icon-chevron-right"></i></a>'+
								'</li>'+
							'</ul>'+
	    			'</div>'+
	    			'<div class="span9">'+
	      	    '<h2 class="col-head no-padding">{{currentSubMenuLabel}}</h2>'+
	     	    	'<div ng-transclude></div>'+
	    			'</div>'+
	        '</div>'+
        '</div>'
  }
} ]);
