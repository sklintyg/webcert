/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
angular.module('webcert').directive('wcValjUtkastTyp',
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
