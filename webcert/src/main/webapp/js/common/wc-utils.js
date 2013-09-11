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
