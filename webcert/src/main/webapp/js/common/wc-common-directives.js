/**
 * Common directives used in both MI as well as in modules. Since this js will be used/loaded from 
 * different contextpaths, all templates are inlined. PLEASE keep source formatting in this 
 * file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('wc.common.directives', []);

angular.module('wc.common.directives').directive("wcHeader", ['$rootScope', function($rootScope) {
    return {
        restrict : "A",
        replace : true,
        scope : {
          userName: "@"
        },
        template :
              '<span class="wc-header">' 
            + ' <a href="/web/start"><img alt="alt text" id="logo" src="/img/webcert_logo.png" /></a>'
            + ' <div id="status">'
            + '     <div class="status-row" ng-show="userName.length">'
            + '         <span message key="nav.label.loggedinas"></span><br><span class="logged-in">{{userName}}</span>'
            + '     </div>'
            + ' </div>'
            + '</span>'
        
    }
} ]);
