"use strict";

/**
 * Common module used in both WC main application as well as in a certificate's module app pages. 
 * Since this js will be used/loaded from different contextpaths, all templates are inlined. PLEASE keep source 
 * formatting in this file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('wc.common', []);
angular.module('wc.common').factory('statService', [ '$http', '$log', '$timeout', '$rootScope', function($http, $log, $timeout, $rootScope) {

    var timeOutPromise = undefined;
    var msPollingInterval = 10* 1000; 
    /*
     * get stats from server
     */
    function _refreshStat() {
        $log.debug("_getStat");
        $http.get('/moduleapi/stat/').success(function(data) {
            $log.debug("_getStat success - data:" + data);
            $rootScope.$broadcast('wc-stat-update', data);
            timeOutPromise = $timeout(_refreshStat, msPollingInterval);
        }).error(function(data, status, headers, config) {
            $log.error("_getStat error " + status);
            timeOutPromise = $timeout(_refreshStat, msPollingInterval);
        });
    }
    
    function _startPolling() {
        _refreshStat();
        $log.debug("statService -> Start polling");
    }
    function _stopPolling() {
        if (timeOutPromise) {
            $timeout.cancel(timeOutPromise);
            $log.debug("statService -> Stop polling");
        }
    }
  
    // Return public API for the service
    return {
        startPolling : _startPolling,
        stopPolling : _stopPolling,
    }
} ]);

angular.module('wc.common').directive("wcHeader", ['$rootScope','$location','statService', function($rootScope,$location,statService) {
    return {
        restrict : "A",
        replace : true,
        scope : {
          userName: "@",
          caregiverName: "@",
          careunitName: "@",
          isDoctor: "@",
          defaultActive: "@"
              
        },
        controller: function($scope, $element, $attrs) {
          //Expose "now" as a model property for the template to render as todays date
          $scope.today = new Date();
          $scope.statService = statService;
          $scope.statService.startPolling();
          
          $scope.stat = {
                  userStat: {},
                  unitStat:{}
                  }
        
          $scope.$on("wc-stat-update", function(event, message){
              $scope.stat = message;   
            });
          
          $scope.menuDefs = [
/*				     { // Temporarily removed for v0.5
				       link :'/web/dashboard#/mycert', 
				       label:'Mina osignerade intyg',
				       requires_doctor: false,
				       statNumberId : "stat-usertstat-unsigned-certs-count",
				       getStat: function() { return $scope.stat.userStat.unsignedCerts || ""}
				     },
	*/			     {
				       link :'/web/dashboard#/index', // v0.5. in v1.0 it is unhandled-qa
				       label:'Frågor och svar',
				       requires_doctor: false,
				       statNumberId : "stat-unitstat-unhandled-question-count",
                       getStat: function() { return $scope.stat.unitStat.unhandledQuestions || ""}
				     },
/*				     { // Temporarily removed for v0.5
				       link :'/web/dashboard#/unsigned', 
				       label:'Enhetens osignerade intyg',
				       requires_doctor: false,
				       statNumberId : "stat-unitstat-unsigned-certs-count",
                       getStat: function() { return $scope.stat.unitStat.unsignedCerts || ""}
				     },
*/				     {
				       link :'/web/dashboard#/support/about',
				       label:'Om Webcert',
				       requires_doctor: false,
                       getStat: function() { return ""}
				     }
				    ];
          
/*        	Temporarily removed for v0.5
 * 					var writeCertMenuDef = {
				       link :'/web/dashboard#/index', 
				       label:'Sök/skriv intyg',
				       requires_doctor: false,
                       getStat: function() { return ""}
				     };
          
          if (eval($scope.isDoctor) == true) {
              $scope.menuDefs.splice(0, 0, writeCertMenuDef);
          }
          else {
              $scope.menuDefs.splice(3, 0, writeCertMenuDef);
          }
*/          
          $scope.isActive = function (page) {
          	if (!page) {return false;}
        		
          	page = page.substr(page.lastIndexOf('/') + 1);
        		if (angular.isString($scope.defaultActive)) {
        			if (page == $scope.defaultActive) {
        				return true;
        			}
          	}
          	
          	var currentRoute = $location.path().substr($location.path().lastIndexOf('/') + 1);
            return page === currentRoute;
          };
        },
        template:
        	'<div>'
        		+'<div class="row-fluid header">'
	        		+'<div class="span6">'
	        			+'<div class="row-fluid">'
		        			+'<div class="span12 headerbox">'
			        			+'<span class="headerbox-logo pull-left"><a href="/web/start"><img alt="Till startsidan" src="/img/webcert_logo.png"/></a></span>'
			          		+'<span class="headerbox-date pull-left">'
			        				+'{{today | date:"shortDate"}}'
			        			+'</span>'
	        				+'</div>'
        				+'</div>'
        			+'</div>'
	        		+'<div class="span6 headerbox-user">'
		      			+'<div class="row-fluid">'
			      			+'<div class="span12">'
			        			+'<div class="headerbox-user-profile pull-right" ng-show="userName.length">'
			                        +'<span ng-switch="isDoctor">'
			                        +'<strong ng-switch-when="true">Läkare</strong>'
			                        +'<strong ng-switch-default>Admin</strong>'
			                        +'</span>'
			        				+' - <span class="logged-in">{{userName}}</span><br>'
			        				+'<a ng-href="/saml/logout">Logga ut</a>'
	        					+'</div>'
			        			+'<div class="headerbox-avatar pull-right">'
			        				+'<img src="/img/avatar.png"/>'
			        			+'</div>'
				      			+'<div class="pull-right location">'
				      				+'<span class="">{{caregiverName}} - {{careunitName}}</span><br>'
				      			+'</div>'
	            		+'</div>'
	        			+'</div>'
	        		+'</div>'
	    			+'</div>'
	    			+'<div class="row-fluid">'
	    				+'<div class="span12">'
	    					+'<div class="navbar">'
				    	  	+'<div class="navbar-inner">'
				    				+'<div class="container">'
				    		  		+'<a class="btn btn-navbar" data-toggle="collapse" data-target=".navbar-responsive-collapse">'
				    						+'<span class="icon-bar"></span>'
				    						+'<span class="icon-bar"></span>'
				    						+'<span class="icon-bar"></span>'
				    					+'</a>'
				    					+'<div class="nav-collapse collapse navbar-responsive-collapse">'
				    						+'<ul class="nav">'
			    								+'<li ng-class="{active: isActive(menu.link)}" ng-repeat="menu in menuDefs">'
		    										+'<a ng-href="{{menu.link}}" ng-show="(menu.requires_doctor && isDoctor) || !menu.requires_doctor">{{menu.label}}'
		    										+'<span id="{{menu.statNumberId}}" ng-if="menu.getStat()>0" class="stat-circle stat-circle-active"'
		    											+'title="Vårdenheten har {{menu.getStat()}} ohanterade frågor och svar.">{{menu.getStat()}}</span></a>'
		    									+'</li>'
		            				+'</ul>'
		            			+'</div><!-- /.nav-collapse -->'
		            		+'</div>'
				    		  +'</div><!-- /navbar-inner -->'
				    	  +'</div>'
	          	+'</div>'	
	    			+'</div>'
	    		+'</div>'
    };
} ]);


angular.module('wc.common').directive("wcSpinner", ['$rootScope', function($rootScope) {
    return {
        restrict : "A",
        transclude : true,
        replace : true,
        scope : {
          label: "@",
          showSpinner: "=",
          showContent: "="
        },
        template :
            '<div>'
           +'  <div ng-show="showSpinner" class="wc-spinner">'
           +'    <img aria-labelledby="loading-message" src="/img/ajax-loader.gif"/>'
           +'    <p id="loading-message">'
           +'      <strong><span message key="{{ label }}"></span></strong>'
           +'    </p>'
           +'  </div>'
           +'  <div ng-show="showContent">'
           +'    <div ng-transclude></div>'
           +'  </div>'
           +'</div>'
    };
} ]);

