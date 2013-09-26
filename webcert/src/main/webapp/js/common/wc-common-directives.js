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
          menuDefs: "@"
              
        },
        controller: function($scope, $element, $attrs) {
            //Expose "now" as a model property for the template to render as todays date
            $scope.today = new Date();
            $scope.menuItems = eval($scope.menuDefs);
            //alert($location.path());
            
            $scope.isActive = function (page) {
              /*if (angular.isString($scope.defaultActive)) {
                  if (page == $scope.defaultActive) {
                      return 'active';
                  }
              }*/
            	
            	page = page.substr(page.lastIndexOf('/') + 1);
              var currentRoute = $location.path().substring(1) || 'index';
              return page === currentRoute;
          };              
        },
        template:
        	'<div>'
        		+'<div class="row-fluid header">'
	        		+'<div class="span6">'
	        			+'<div class="row-fluid">'
		        			+'<div class="span12">'
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
			        				+'<span><a tabindex="-1" href="#">Logga ut</a></span>'
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
    }
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
           +'  <div ng-show="showSpinner" style="text-align: center; padding: 20px;">'
           +'    <img aria-labelledby="loading-message" src="/img/ajax-loader.gif" style="text-align: center;" />'
           +'    <p id="loading-message" style="text-align: center; color: #64ABC0; margin-top: 20px;">'
           +'      <strong><span message key="{{ label }}"></span></strong>'
           +'    </p>'
           +'  </div>'
           +'  <div ng-show="showContent">'
           +'    <div ng-transclude></div>'
           +'  </div>'
           +'</div>'
    }
} ]);

angular.module('wc.common.directives').directive("wcCertField", ['$rootScope', function($rootScope) {
  return {
      restrict : "A",
      transclude : true,
      replace : true,
      scope : {
        fieldLabel: "@",
        fieldNumber: "@",
        filled: "=?"
      },
      template :
      		'<div class="body-row clearfix">'
      			+'<h4 class="cert-field-number"><span message key="view.label.field"></span> {{fieldNumber}}</h4>'
      			+'<h3 class="title" ng-class="{ \'unfilled\' : !filled}"><span message key="{{ fieldLabel }}"></span> <span class="cert-field-blank" ng-hide="filled"><span message key="view.label.blank"></span></span></h3>'
      			+'<span class="text" ng-show="filled">'
      			+'<span ng-transclude></span>'
      			+'</span>'
         +'</div>'


/*
	//TODO: fix this to use default value for filled if it is omitted (it is already optional but default value is undefined =?)
	compile: function(element,attrs)
    {
	    var filled = attrs.filled || true;
	    var htmlText = '<div class="body-row">'
	               +'   <h3 class="title" ng-class="{ \'unfilled\' : !'+filled+'}"><span message key="{{ title }}"></span> <span ng-hide="'+filled+'"><span message key="view.label.blank"></span></span></h3>'
	               +'   <span class="text" ng-show="'+filled+'">'
	               +'       <span ng-transclude></span>'
	               +'   </span>'
	               +'</div>';
	    element.replaceWith(htmlText);
    }*/
  }
} ]);