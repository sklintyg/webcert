'use strict';

/**
 * message directive for externalizing text resources.
 *
 * All resourcekeys are expected to be defined in lowercase and available in a
 * global js object named "messages"
 * Also supports dynamic key values such as key="status.{{scopedvalue}}"
 *
 * Usage: <message key="some.resource.key" [fallback="defaulttextifnokeyfound"]/>
 */
angular.module('modules.messages', []).factory("messageService", ['$rootScope', function ($rootScope) {

    var _messageResources = null;

    function _getProperty(key, language, defaultValue, fallbackToDefaultLanguage) {
        var value;
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

    function _getPropertyInLanguage(lang, key) {
        _checkResources();
        return _lookupProperty(_messageResources[lang], key);
    }

    function _lookupProperty(resources, key) {
        return resources[key];
    }

    function _addResources(resources) {
        _checkResources();
        angular.extend(_messageResources.sv, resources.sv);
        angular.extend(_messageResources.en, resources.en);
    }

    function _checkResources() {
        if(_messageResources==null) {
            _messageResources = {
                "sv": {
                    "initial.key": "Initial nyckel"
                },
                "en": {
                    "initial.key": "Initial key"
                }
            };
        }
    }

    return {
        getProperty: _getProperty,
        addResources: _addResources
    }

}]).directive("message", ['$rootScope', 'messageService', function ($rootScope, messageService) {

    return {
        restrict: "E",
        scope: true,
        replace: true,
        template: "<span ng-bind-html-unsafe='resultValue'></span>",
        link: function ($scope, element, attr) {
            var lang = $rootScope.lang;
            // observe changes to interpolated attribute
            attr.$observe('key', function (interpolatedKey) {
                var normalizedKey = angular.lowercase(interpolatedKey);
                var useLanguage;
                if (typeof attr.lang !== "undefined") {
                    useLanguage = attr.lang;
                } else {
                    useLanguage = $rootScope.lang;
                }
                // now get the value to display..
                $scope.resultValue = messageService.getProperty(normalizedKey, useLanguage, attr.fallback, (typeof attr.fallbackDefaultLang !== "undefined"));
            });
        }
    }

} ]);

