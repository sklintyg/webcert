'use strict';
/**
 * Common util components and services for cross cutting concerns in the app.
 * 
 * @author marced
 * 
 */

/**
 * Interceptor that decorates all GET requests made by the $http service. Can be
 * used by the modules as a common component. To hook up the interceptor, simply
 * config the http provider for the app like this (in 1.1.5):
 * 
 * app.config(function ($httpProvider) {
 * $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster'); })
 * 
 */
angular.module('wc.utils', []);
angular.module('wc.utils').factory('httpRequestInterceptorCacheBuster', function($q) {
    return {
        request : function(config) {
            // Don't mess with view loading, ok if cached..
            if (config.url.indexOf(".html") == -1) {
                var sep = config.url.indexOf('?') === -1 ? '?' : '&';
                config.url = config.url + sep + 'cacheSlayer=' + new Date().getTime();
            }
            return config || $q.when(config);
        }
    };
});

/**
 * Response intercepter catching ALL responses coming back through the $http
 * service. On 403 status responses, the browser is redirected to the web apps
 * main starting point. To hook up the interceptor, simply config the http
 * provider for the app like this (in 1.1.5):
 * 
 * $httpProvider.responseInterceptors.push('http403ResponseInterceptor');
 * 
 * The url which the interceptor redirects to on a 403 response can be
 * configured via the providers setRedirectUrl in the apps config block, e.g:
 * 
 * http403ResponseInterceptorProvider.setRedirectUrl("/web/403-error.jsp");
 */
angular.module('wc.utils').provider('http403ResponseInterceptor', function() {

    /**
     * Object that holds config and default values.
     */
    this.config = {
        redirectUrl : "/"
    };

    /**
     * Setter for configuring the redirectUrl
     */
    this.setRedirectUrl = function(url) {
        this.config.redirectUrl = url;
    }

    /**
     * Mandatory provider $get function. here we can inject the dependencies the
     * actual implementation needs, in this case $q (and $window for redirection)
     */
    this.$get = [ '$q', '$window', function($q, $window) {
        //Ref our config object
        var config = this.config;
        // Add our custom success/failure handlers to the promise chain..
        function interceptorImpl(promise) {
            return promise.then(function(response) {
                // success - simply return response as-is..
                return response;
            }, function(response) {
                // for 403 responses - redirect browser to configured redirect url
                if (response.status == "403") {
                    $window.location.href = config.redirectUrl;
                }
                // signal rejection (arguably not meaningful here since we just
                // issued a redirect)
                return $q.reject(response);
            });
        }
        return interceptorImpl;

    } ];

});
/*
 * Generic Dialog services
 */
angular.module('wc.utils').factory(
        'wcDialogService',
        [
                '$http',
                '$log',
                '$modal',
                function($http, $log, $modal) {

                    function _showErrorMessageDialog(message, callback) {

                        var msgbox = $modal.open({
                            template : 
                                ' <div class="modal-header">' 
                                + '<h3>Tekniskt fel</h3>' 
                                + '</div>' 
                                + '<div class="modal-body">' 
                                + ' {{bodyText}}' 
                                + '</div>' 
                                + '<div class="modal-footer">'
                                + ' <button class="btn btn-success" ng-click="$close()">OK</button>' 
                                + '</div>',
                            controller : function($scope, $modalInstance, bodyText) {
                                $scope.bodyText = bodyText;
                            },
                            resolve : {
                                bodyText : function() {
                                    return angular.copy(message);
                                }
                            }
                        });

                        msgbox.result.then(function(result) {
                            if (callback) {
                                callback(result)
                            }
                        }, function() {
                        });
                    }
                    // Return public API for the service
                    return {
                        showErrorMessageDialog : _showErrorMessageDialog
                    }

                } ]);

// Common module utils
angular.module('wc.utils').directive("wcField", [function() {
  return {
    restrict : "A",
    transclude : true,
    replace : true,
    scope : {
      fieldLabel: "@",
      fieldNumber: "@",
      fieldHelpText: "=",
      fieldHasErrors: "="
    },
    template :
        '<div class="body-row clearfix">'
            +'<h4 class="cert-field-number" ng-if="fieldNumber != undefined"><span message key="modules.label.field"></span> {{fieldNumber}}</h4>'
            +'<h3 class="title"><span message key="{{fieldLabel}}"></span><span ng-if="fieldHelpText != undefined" class="help" tooltip-trigger="click" tooltip-html-unsafe="{{fieldHelpText}}">?</span></h3>'
            +'<span class="text" ng-class="{fielderror: fieldHasErrors}">'
            +'  <span ng-transclude></span>'
            +'</span>'
       +'</div>'
  }
} ]);

angular.module('wc.utils').directive("wcFieldSingle", [function() {
  return {
    restrict : "A",
    transclude : true,
    replace : true,
    scope : {
      fieldNumber: "@"
    },
    template :
        '<div class="body-row body-row-single clearfix">'
            +'<h4 class="cert-field-number" ng-if="fieldNumber != undefined"><span message key="modules.label.field"></span> {{fieldNumber}}</h4>'
            +'<span ng-transclude></span>'
        +'</div>'
  }
} ]);
