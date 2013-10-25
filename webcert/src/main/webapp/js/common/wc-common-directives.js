"use strict";

/**
 * Common directives used in both WC main application as well as in a certificate's module app pages. 
 * Since this js will be used/loaded from different contextpaths, all templates are inlined. PLEASE keep source 
 * formatting in this file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('wc.common.directives', []);
angular.module('wc.common.directives').directive("wcHeader", ['$rootScope','$location', function($rootScope,$location) {
    return {
        restrict : "A",
        replace : true,
        scope : {
          userName: "@",
          caregiverName: "@",
          isDoctor: "@",
          menuDefs: "@",
          defaultActive: "@"
              
        },
        controller: function($scope, $element, $attrs) {
          //Expose "now" as a model property for the template to render as todays date
          $scope.today = new Date();
          
          var defaultMenuDefs = [
				     {
				       link :'/web/dashboard#/create', 
				       label:'Sök/skriv intyg',
				       requires_doctor: false
				     },
				     {
				       link :'/web/dashboard#/index', 
				       label:'Mina osignerade intyg',
				       requires_doctor: true
				     },
				     {
				       link :'/web/dashboard#/unhandled-qa',
				       label:'Enhetens frågor och svar',
				       requires_doctor: false
				     },
				     {
				       link :'/web/dashboard#/unsigned', 
				       label:'Enhetens osignerade intyg',
				       requires_doctor: false
				     },
				     {
				       link :'/web/dashboard#/about.support',
				       label:'Om Webcert',
				       requires_doctor: false
				     }
				    ];
          
          $scope.menuItems = defaultMenuDefs;
          if($scope.menuDefs != undefined && $scope.menuDefs != ''){
          	$scope.menuItems = eval($scope.menuDefs);
          }
          
          $scope.isActive = function (page) {
          	if (!page) {return false;}
          	 page = page.substr(page.lastIndexOf('/') + 1);
          	 if (angular.isString($scope.defaultActive)) {
               if (page == $scope.defaultActive) {
                   return 'active';
               }
          	 }
          	
          	var hasSubMenu = page.lastIndexOf('.') > -1;

            var currentRoute = $location.path().substring(1) || 'index';
          	if(hasSubMenu) {
          		page = page.substring(0, page.lastIndexOf('.'));
          		currentRoute = currentRoute.substring(0, currentRoute.lastIndexOf('.'));
          	}
          	
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
			        			+'<div class="pull-left"><img src="/img/avatar.png"/></div>'
			        			+'<div class="headerbox-user-profile pull-left" ng-show="userName.length">'
			                        +'<span ng-switch="isDoctor">'
			                        +'<strong ng-switch-when="true">Läkare</strong>'
			                        +'<strong ng-switch-default>Admin</strong>'
			                        +'</span>'
			        				+' - <span class="logged-in">{{userName}}</span><br>'
			        				+'<span class="location">{{caregiverName}}</span><br>'
	        					+'</div>'
	        					+'<button class="logout-button" ng-click="">Logga ut</button>'
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
			    								+'<li ng-class="{active: isActive(menu.link)}" ng-repeat="menu in menuItems">'
		    										+'<a ng-href="{{menu.link}}" ng-show="(menu.requires_doctor && isDoctor) || !menu.requires_doctor">{{menu.label}}</a>'
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


angular.module('wc.common.directives').directive("wcSpinner", ['$rootScope', function($rootScope) {
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

