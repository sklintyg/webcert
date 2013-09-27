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
        			'<!--<td><span class="qa-circle qa-circle-active" title="Ohanterade frågor och svar">2</span></td>-->'+
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