angular.module('webcert').directive('wcValjUtkastTyp',
    ['webcert.SokSkrivIntygViewstate', 'webcert.IntygTypeSelectorModel', 'common.messageService', 'common.featureService',
    function(ViewState, IntygTypeSelectorModel, messageService, featureService) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                viewState: '='
            },
            templateUrl: '/app/views/sokSkrivIntyg/valjUtkastTyp.directive.html',
            link: function(scope, element, attrs) {
                scope.intygTypeModel = IntygTypeSelectorModel.build();
                scope.intygReplacement = {
                    'fk7263':'lisjp'
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
                    // Måste ha valt en intygstyp
                    // Intygstypen får inte vara deprecated
                    // Måste uppfylla ev unikhetskrav för intyg/utkast inom/utom vg
                    return IntygTypeSelectorModel.intygType !== 'default' &&
                        !scope.intygReplacement[IntygTypeSelectorModel.intygType] &&
                        scope.passesUniqueIntygWithinCareGiverCheck(IntygTypeSelectorModel.intygType) &&
                        scope.passesUniqueUtkastWithinCareGiverCheck(IntygTypeSelectorModel.intygType) &&
                        scope.passedUniqueGlobalCheck(IntygTypeSelectorModel.intygType);
                };

                // Har intygstypen begränsning som säger att inga andra intyg av typen får finnas på samma vg för patienten?
                scope.passesUniqueIntygWithinCareGiverCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG_INOM_VG, intygType);
                    return featureActive ? !IntygTypeSelectorModel.previousIntygWarnings[intygType] : true;
                };
                // Har intygstypen begränsning som säger att inga andra utkast av typen får finnas på samma vg för patienten?
                scope.passesUniqueUtkastWithinCareGiverCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_UTKAST_INOM_VG, intygType);
                    return featureActive ? !IntygTypeSelectorModel.previousUtkastWarnings[intygType] : true;
                };

                // Har intygstypen begränsning som säger att inga andra intyg av typen får finnas utfärdade på någon vg för patienten?
                scope.passedUniqueGlobalCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG, intygType);
                    return featureActive ? IntygTypeSelectorModel.previousIntygWarnings[intygType] === undefined : true;
                };
            }
        };
    }]
);
