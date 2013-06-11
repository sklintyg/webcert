/**
 * Common directives used in both MI as well as in modules. Since this js will be used/loaded from 
 * different contextpaths, all templates are inlined. PLEASE keep source formatting in this 
 * file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('directives.mi', []);

angular.module('directives.mi').directive("miHeader", ['$rootScope', function($rootScope) {
    return {
        restrict : "E",
        replace : true,
        scope : {
          userName: "@"
        },
        template :
              '<span class="mi-header">' //directives must have a single root element.
            + ' <a href="/web/start"><img id="logo" src="/img/logo.png" /></a>'
            + ' <div id="status">'
            + '     <div class="status-row">'
            + '         <message key="nav.label.loggedinas"></message><br><span class="logged-in">{{userName}}</span>'
            + '     </div>'
            + ' </div>'
            + '</span>'
        
    }
} ]);

angular.module('directives.mi').directive("miMainNavigation", ['$rootScope', '$location' , function($rootScope, $location) {
    return {
        restrict : "E",
        replace : true,
        scope : {
            linkPrefix: "@",
            defaultActive: "@"
          },
        controller: function($scope, $element, $attrs) {
            $scope.navClass = function (page) {
                if (angular.isString($scope.defaultActive)) {
                    if (page == $scope.defaultActive) {
                        return 'active';
                    }
                }
                var currentRoute = $location.path().substring(1) || 'lista';
                return page === currentRoute ? 'active' : '';
            };  
        },
        template :
            '<div class="navbar mi-main-navigation">'
            + '<div class="navbar-inner">'
            + '  <ul class="nav">'
            + '    <li ng-class="navClass(\'lista\')"><a ng-href="{{linkPrefix}}#/lista" id="inboxTab"><message key="nav.label.inbox"></message></a></li>'
		    + '    <li class="divider-vertical"></li>'
            + '    <li ng-class="navClass(\'arkiverade\')"><a ng-href="{{linkPrefix}}#/arkiverade" id="archivedTab"><message key="nav.label.archived"></message></a></li>'
            + '    <li class="divider-vertical"></li>'
            + '    <li ng-class="navClass(\'omminaintyg\')"><a ng-href="{{linkPrefix}}#/omminaintyg" id="aboutTab"><message key="nav.label.aboutminaintyg"></message></a></li>'
            + '    <li class="divider-vertical"></li>'
            + '    <li ng-class="navClass(\'hjalp\')"><a ng-href="{{linkPrefix}}#/hjalp" id="helpTab"><message key="nav.label.help"></message></a></li>'
            + '  </ul>'
            + ' </div>'
            + '</div>'
            
            
    }
} ]);

angular.module('directives.mi').directive("mvkTopBar", ['$rootScope', '$location' , function($rootScope, $location) {
    return {
        restrict : "E",
        replace : true,
        template :
              '<div id="headerContainer">'
            + ' <div id="header">'
            + '  <div class="wrapper">'
            + '   <a href="####" class="backButton" id="backToMvkLink">'
            + '     <h1 class="assistiveText"><message key="mvk.header.linktext"></message></h1>'
            + '   </a>'
            + '   <div class="functionRow">'
            + '    <a href="####"  id="mvklogoutLink"><message key="mvk.header.logouttext"></message></a>'
            + '   </div>'
            + '   <div class="clear"></div>'
            + '  </div>'
            + ' </div>'
            + '</div>'
    }
} ]);

