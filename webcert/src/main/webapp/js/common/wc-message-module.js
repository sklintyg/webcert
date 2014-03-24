define([
    'angular'
], function (angular) {
    'use strict';

    var moduleName = 'modules.messages';

    /**
     * message directive for externalizing text resources.
     *
     * All resourcekeys are expected to be defined in lowercase and available in a
     * global js object named "messages"
     * Also supports dynamic key values such as key="status.{{scopedvalue}}"
     *
     * Usage: <message key="some.resource.key" [fallback="defaulttextifnokeyfound"]/>
     */
    var messages = angular.module(moduleName, []).factory("messageService", ['$rootScope', function ($rootScope) {

            var _messageResources = null;

            function _getProperty (key, language, defaultValue, fallbackToDefaultLanguage) {
                var value;

                if (typeof language === "undefined") {
                    language = $rootScope.lang;
                }

                value = _getPropertyInLanguage(language, key);
                if (typeof value === "undefined") {
                    // use fallback attr value if defined
                    if (fallbackToDefaultLanguage) {
                        value = messageService._getPropertyInLanguage($rootScope.DEFAULT_LANG, key);
                    }
                    if (typeof value === "undefined") {
                        // use fallback attr value if defined
                        value = (typeof defaultValue === "undefined") ? "[Missing '" + key + "']" : defaultValue;
                    }
                }
                return value;
            }

            function _getPropertyInLanguage (lang, key) {
                _checkResources();
                return _lookupProperty(_messageResources[lang], key);
            }

            function _lookupProperty (resources, key) {
                return resources[key];
            }

            function _addResources (resources) {
                _checkResources();
                angular.extend(_messageResources.sv, resources.sv);
                angular.extend(_messageResources.en, resources.en);
            }

            function _checkResources () {
                if (_messageResources == null) {
                    _messageResources = {
                        "sv" : {
                            "initial.key" : "Initial nyckel"
                        },
                        "en" : {
                            "initial.key" : "Initial key"
                        }
                    };
                }
            }

            return {
                getProperty : _getProperty,
                addResources : _addResources
            }

        }]).directive("message", ['$rootScope', 'messageService', function ($rootScope, messageService) {

            return {
                restrict : "A",
                scope : {
                    "key" : "@",
                    "param" : "=",
                    "params" : "="
                },
                replace : true,
                template: "<span ng-bind-html='resultValue'></span>",
                link : function (scope, element, attr) {
                    var result;
                    // observe changes to interpolated attribute
                    attr.$observe('key', function (interpolatedKey) {
                        var normalizedKey = angular.lowercase(interpolatedKey);
                        var useLanguage;
                        if (typeof attr.lang !== "undefined") {
                            useLanguage = attr.lang;
                        } else {
                            useLanguage = $rootScope.lang;
                        }

                        result = messageService.getProperty(normalizedKey, useLanguage, attr.fallback, (typeof attr.fallbackDefaultLang !== "undefined"));

                        if (typeof scope.param !== "undefined") {
                            console.log(scope.param);
                            result = result.replace("%0", scope.param);
                        } else {
                            if (typeof scope.params !== "undefined") {
                                var myparams = scope.params;
                                for (var i = 0; i < myparams.length; i++) {
                                    result = result.replace("%" + i, myparams[i]);
                                }
                            }
                        }

                        // now get the value to display..
                        scope.resultValue = result;
                    });
                }
            }

        } ]);

    return moduleName;
});