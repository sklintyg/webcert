angular.module('webcert').directive('valjUtkastTyp',
    ['webcert.SokSkrivIntygViewstate', 'webcert.IntygTypeSelectorModel', 'common.UserModel', 'common.messageService', 'common.featureService',
    function(ViewState, IntygTypeSelectorModel, UserModel, messageService, featureService) {
        'use strict';

        return {
            restrict: 'E',
            scope: {},
            templateUrl: '/app/views/sokSkrivIntyg/valjUtkastTyp.directive.html',
            link: function(scope, element, attrs) {
                scope.intygTypeModel = IntygTypeSelectorModel.build();
                scope.intygReplacement = {
                    'fk7263':'lisjp'
                };
                scope.isNormalOrigin = function () {
                    //Closure needed, due to 'this' reference in UserModel.isNormalOrigin().
                    return UserModel.isNormalOrigin();
                };
                scope.resolveIntygReplacedText = function (selectedIntygType) {
                    var selectedIntyg = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === selectedIntygType);
                    })[0];
                    var replacedIntygsType = scope.intygReplacement[selectedIntygType];
                    var replacedIntyg = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === replacedIntygsType);
                    })[0];
                    return messageService.getProperty('info.intygstyp.replaced', {oldIntygstyp: selectedIntyg.label, newIntygstyp: replacedIntyg.label});
                };
                scope.getDetailedDescription = function (intygsType) {
                    var intygTypes = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === intygsType);
                    });
                    if (intygTypes && intygTypes.length > 0) {
                        return intygTypes[0].detailedDescription;
                    }
                };
                scope.showCreateUtkast = function () {
                    return !(IntygTypeSelectorModel.intygType === 'default' ||
                        (scope.intygReplacement[IntygTypeSelectorModel.intygType] && UserModel.isNormalOrigin()) ||
                        scope.isUniqueWithinCareGiver(IntygTypeSelectorModel.intygType) ||
                        scope.isUniqueGlobal(IntygTypeSelectorModel.intygType));
                };
                scope.isUniqueWithinCareGiver = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG_INOM_VG, intygType);
                    return featureActive && IntygTypeSelectorModel.previousIntygWarnings[intygType];
                };
    
                scope.isUniqueGlobal = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG, intygType);
                    return featureActive && IntygTypeSelectorModel.previousIntygWarnings[intygType] !== undefined;
                };
            }
        };
    }]
);