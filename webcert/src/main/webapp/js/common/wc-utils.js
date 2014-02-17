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

                    /*
                      showDialog parameters:

                      scope = parent scope

                      options =
                         dialogId: html id attribute of dialog
                         titleId: message id of title text
                         bodyTextId: message id of body text
                         bodyText: body text (can be used instead of or in addition to bodyTextId
                         button1id: (optional) html id attribute of button 1
                         button2id: (optional) html id attribute of button 2
                         button3id: (optional) html id attribute of button 3
                         button1click: (optional) function on button 1 click
                         button2click: (optional) function on button 2 click
                         button3click: (optional) function on button 3 click
                         button1text: (optional) message id on button 1 text. default: OK
                         button2text: (optional) message id on button 2 text. default: Cancel
                         button3text: (optional) message id on button 3 text. default: No, don't ask
                         button3visible: (optional) whether button 3 should be visible. default: true if button3text is specified, otherwise false
                         autoClose: whether dialog should close on button click. If false, use .close() on return value from showDialog to close dialog later

                     */
                    function _showDialog(scope, options) {

                      // Apply default dialog behaviour values
                      scope.dialog = {
                        acceptprogressdone: true,
                        focus: false
                      };

                      // setup options defaults if parameters aren't included
                      options.dialogId = (options.dialogId == undefined) ? "id"+Math.floor(Math.random()*11) : options.dialogId;
                      options.bodyText = (options.bodyText == undefined) ? "" : options.bodyText;
                      options.button1text = (options.button1text == undefined) ? "common.ok" : options.button1text;
                      options.button2text = (options.button2text == undefined) ? "common.cancel" : options.button2text;
                      options.button3text = (options.button3text == undefined) ? undefined : options.button3text;
                      options.button3visible = options.button3visible == undefined ? options.button3text != undefined : options.button3visible;
                      options.button1id = (options.button1id == undefined) ? "button1"+Math.floor(Math.random()*11) : options.button1id;
                      options.button2id = (options.button2id == undefined) ? "button2"+Math.floor(Math.random()*11) : options.button2id;
                      options.button3id = (options.button3id == undefined) ? "button3"+Math.floor(Math.random()*11) : options.button3id;
                      options.autoClose = (options.autoClose == undefined) ? true : options.autoClose;

                      // Create controller to setup dialog
                      var DialogInstanceCtrl = function ($scope, $modalInstance, dialogId, titleId, bodyTextId, bodyText, button1id,button2id,button3id, button1click, button2click, button3click, button3visible, button1text, button2text, button3text, autoClose) {

                        $scope.dialogId = dialogId;
                        $scope.titleId = titleId;
                        $scope.bodyTextId = bodyTextId;
                        $scope.bodyText = bodyText;
                        $scope.button1click = function(result) {
                          button1click();
                          if(autoClose) {
                            $modalInstance.close(result)
                          }
                        };
                        $scope.button2click = function(result) {
                          if(button2click) button2click();
                          $modalInstance.dismiss('button2 dismiss')
                        };
                        $scope.button3visible = button3visible;
                        if($scope.button3visible != undefined){
                          $scope.button3click = function(result) {
                            if(button3click) button3click();
                            $modalInstance.dismiss('button3 dismiss') };
                        } else {
                          $scope.button3visible = false;
                        }
                        $scope.button1text = button1text;
                        $scope.button2text = button2text;
                        $scope.button3text = button3text;
                        $scope.button1id = button1id;
                        $scope.button2id = button2id;
                        $scope.button3id = button3id;
                      };

                      // Open dialog box using specified options, template and controller
                      var msgbox = $modal.open({
                        scope: scope,
                        templateUrl: '/views/partials/common-dialog.html',
                        controller: DialogInstanceCtrl,
                        resolve: {
                          dialogId: function() { return angular.copy(options.dialogId); },
                          titleId: function() { return angular.copy(options.titleId); },
                          bodyTextId: function() { return angular.copy(options.bodyTextId); },
                          bodyText: function() { return angular.copy(options.bodyText); },
                          button1id: function() { return angular.copy(options.button1id); },
                          button2id: function() { return angular.copy(options.button2id); },
                          button3id: function() { return angular.copy(options.button3id); },
                          button1click: function() { return options.button1click; },
                          button2click: function() { return options.button2click; },
                          button3click: function() { return options.button3click; },
                          button1text: function() { return angular.copy(options.button1text); },
                          button2text: function() { return angular.copy(options.button2text); },
                          button3text: function() { return angular.copy(options.button3text); },
                          button3visible: function() { return angular.copy(options.button3visible); },
                          autoClose: function() { return angular.copy(options.autoClose); }
                        }
                      });

                      msgbox.result.then(function(result) {
                        if (options.callback) {
                          options.callback(result)
                        }
                      }, function() {});

                      return msgbox;
                    }

                    // Return public API for the service
                    return {
                      showErrorMessageDialog : _showErrorMessageDialog,
                      showDialog : _showDialog
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

/**
 * wc-maxlength directive which limits amount of characters that can be entered in a input box/textarea and adds a counter below the element
 * usage:
 *  directive demands the following attributes on the element:
 *  wc-maxlength
 *  name (for unique id for counter scope name)
 *  ng-model (for mapping model)
 *  maxlength (for setting maxlength)
 */
angular.module('wc.utils').directive("wcMaxlength", function($log, $compile) {
  return {
    restrict : "A",
    require: "ngModel",
    link: function(scope, element, attrs, controller) {
      scope["charsRemaining"+element[0].name] = attrs.maxlength;
      var counter = angular.element("<div class='counter'>Tecken kvar: {{charsRemaining"+element[0].name+"}}</div>");
      $compile(counter)(scope);
      element.parent().append(counter);

      function limitLength(text) {
        if(!text) return;
        if (text.length > attrs.maxlength) {
          var transformedInput = text.substring(0, attrs.maxlength);
          controller.$setViewValue(transformedInput);
          controller.$render();
          return transformedInput;
        }
        scope["charsRemaining"+element[0].name] = attrs.maxlength - text.length;
        return text;
      }
      controller.$formatters.unshift(limitLength);
      controller.$parsers.unshift(limitLength);
    }
  }
});
