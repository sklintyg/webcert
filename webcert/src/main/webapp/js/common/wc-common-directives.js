/**
 * Common directives used in both WC main application as well as in a certificate's module app pages. 
 * Since this js will be used/loaded from different contextpaths, all templates are inlined. PLEASE keep source 
 * formatting in this file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('wc.common.directives', []);
angular.module('wc.common.directives').directive("wcHeader", ['$rootScope', function($rootScope) {
    return {
        restrict : "A",
        replace : true,
        scope : {
          userName: "@"
        },
        controller: function($scope, $element, $attrs) {
            //Expose "now" as a model property for the template to render as todays date
            $scope.today = new Date();
        },
        template:
        	'<div>'
        	+'<div class="row-fluid">'
        		+'<div class="span2">'
        			+'<div class="headerbox-logo"><a href="/web/start"><img alt="Till startsidan" src="/img/webcert_logo.png"/></a></div>'
        		+'</div>'
        		+'<div class="span4 headerbox-date">'
        			+'{{today | date:"shortDate"}}'
        		+'</div>'
        		+'<div class="span3 headerbox-user">'
        			+'<div class="span2"><img src="/img/avatar.png"/></div>'
        			+'<div class="span10" ng-show="userName.length">'
        				+'<strong>Läkare</strong> - <span class="logged-in">{{userName}}</span><br>'
        				+'<span class="location">ABC Landstinget Västmanland</span>'
        			+'</div>'
        		+'</div>'
	        	+'<div class="span3">'
		    			+'<div class="dropdown">'
		    				+'<a class="dropdown-toggle settings" data-toggle="dropdown" href="#"></a>'
		    				+'<ul class="dropdown-menu dropdown-menu-center" role="menu" aria-labelledby="dLabel">'
		    					+'<li><a tabindex="-1" href="#">Hjälp</a></li>'
		    					+'<li><a tabindex="-1" href="#">Logga ut</a></li>'
		    				+'</ul>'
		    			+'</div>'				
	    			+'</div>'
    			+'</div>'
    		+'</div>'  
    }
} ]);
