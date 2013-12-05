'use strict';

/* Directives */
angular.module('wcDashBoardApp').directive("wcCareUnitClinicSelector", ['$rootScope', '$cookieStore', 'dashBoardService', function ($rootScope, $cookieStore, dashBoardService) {
    return {
        restrict: "A",
        transclude: false,
        replace: true,
        template: 
    	    '<table class="span12 table unit-table">'+
        		'<tr ng-repeat="unit in units">'+
        			'<td><button id="select-active-unit-{{unit.id}}" type="button" ng-click="selectUnit(unit)" class="qa-unit" ng-class="{selected : selectedUnit == unit}">{{unit.namn}}<span class="qa-circle" ng-class="{\'qa-circle-active\': getItemCountForUnitId(unit)>0}" title="Ohanterade frågor och svar">{{getItemCountForUnitId(unit)}}</span></button></td>'+
        		'</tr>'+
        	'</table>',
        controller: function ($scope) {
            // init
            $scope.vardenheter = angular.copy($rootScope.MODULE_CONFIG.USERCONTEXT.vardgivare[0].vardenheter);

            $scope.units = []; // aggregated units to present for vardenhet/mottagning choice
            
            angular.forEach($scope.vardenheter, function(vardenhet, key){
              this.push(vardenhet);
              
              angular.forEach(vardenhet.mottagningar, function(mottagning, key){
              	mottagning.namn = vardenhet.namn + ' - ' + mottagning.namn;
              	this.push(mottagning);
              }, $scope.units);
              
            }, $scope.units);
            
            $scope.selectedUnit = null;

            $scope.selectUnit = function(unit) {
            	$scope.selectedUnit = unit;
            	$rootScope.$broadcast('select-care-unit', $scope.selectedUnit);
            }
            
            //initial selection
            if($scope.units.length == 1) {
            	$scope.selectUnit(selectFirstUnit($scope.units));
            }else if($scope.units.length > 1 && $cookieStore.get("enhetsId"))   {
                $scope.selectUnit(selectUnitById($scope.units,$cookieStore.get("enhetsId")) ) ;
            }


            // Local function getting the first care unit's hsa id in the data struct.
            function selectFirstUnit(units) {
                if (typeof units === "undefined" || units.length == 0) {
                    return null;
                } else {
                    return units[0];
                }
            }

            function selectUnitById(units, unitName){
                for(var count =0;count<units.length;count++){
                      if(units[count].id==unitName){
                          return units[count];
                      }
                   }
                return selectFirstUnit(units);
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
	        	link :'/web/dashboard#/support/about', 
	          label:'Support'
	        },
	        {
	        	link :'/web/dashboard#/webcert/about', 
	          label:'Om Webcert'
	        }
        ];
        
        function getSubMenuName(path) {
          var path = path.substring(0, path.lastIndexOf('/'));
        	return path.substring(path.lastIndexOf('/') + 1); 
        }
        
        var currentSubMenuName = getSubMenuName($location.path()) || 'index';
        $scope.currentSubMenuLabel = "";

        // set header label based on menu items label
        angular.forEach($scope.menuItems, function(menu, key) {
        	var page = getSubMenuName(menu.link);
        	if(page == currentSubMenuName) {
        		$scope.currentSubMenuLabel = menu.label; 
        	}
        });
        
        $scope.isActive = function (page) {
        	page = getSubMenuName(page);
          return page === currentSubMenuName;
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
	    			'<div class="span9 about-content">'+
	      	    '<h2 class="col-head col-head-about">{{currentSubMenuLabel}}</h2>'+
	     	    	'<div ng-transclude></div>'+
	    			'</div>'+
	        '</div>'+
        '</div>'
  }
} ]);
