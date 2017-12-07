/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcFragorOchSvarList', [
    '$rootScope', '$cookies', '$filter', '$location', '$log', '$scope', '$timeout', '$window',
    'common.enhetArendenCommonService', 'common.ArendeVidarebefordraHelper', 'common.ArendeProxy', 'common.dialogService',
    'webcert.enhetArendenService',
    function($cookies, $filter, $location, $log, $timeout, $window, enhetArendenCommonService, ArendeVidarebefordraHelper, ArendeProxy, dialogService, enhetArendenService) {
        'use strict';

        return {
            restrict: 'E',
            transclude: false,
            replace: false,
            scope: {

            },
            templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenList/wcEnhetArendenList.directive.html',
            controller: function($scope) {

                $scope.widgetState = {
                    doneLoading: true,
                    runningQuery: false,
                    activeErrorMessageKey: null,
                    totalCount: 0,
                    fetchingMoreInProgress: false,
                    currentList: []
                };

                $scope.fetchMore = function() {
                    $log.debug('fetchMore');
                    $scope.filterQuery.startFrom += $scope.filterQuery.pageSize;
                    $scope.widgetState.fetchingMoreInProgress = true;
                    enhetArendenService.getArenden($scope);
                };

                $scope.openIntyg = function(intygId, intygTyp) {
                    $log.debug('open intyg ' + intygId + ' of type ' + intygTyp);
                    $location.url('/fragasvar/' + intygTyp.toLowerCase() + '/' + intygId, true);
                };

                // Handle vidarebefordra dialog
                $scope.openMailDialog = function(arende) {
                    $timeout(function() {
                        ArendeVidarebefordraHelper.handleVidareBefodradToggle(arende, $scope.onVidareBefordradChange);
                    }, 1000);

                    // Launch mail client
                    var arendeMailModel = {
                        intygId: arende.intygId,
                        intygType: arende.intygTyp,
                        enhetsnamn: arende.enhetsnamn,
                        vardgivarnamn: arende.vardgivarnamn
                    };
                    $window.location = ArendeVidarebefordraHelper.buildMailToLink(arendeMailModel);
                };

                $scope.onVidareBefordradChange = function(arende) {
                    arende.updateInProgress = true;
                    $log.debug('onVidareBefordradChange: fragaSvarId: ' + arende.meddelandeId + ' intysTyp: ' +
                        arende.intygTyp);
                    ArendeProxy.setVidarebefordradState(arende.meddelandeId, arende.intygTyp,
                        arende.vidarebefordrad, function(result) {
                            arende.updateInProgress = false;

                            if (result !== null) {
                                arende.vidarebefordrad = result.fraga.vidarebefordrad;
                            } else {
                                arende.vidarebefordrad = !arende.vidarebefordrad;
                                dialogService
                                    .showErrorMessageDialog('Kunde inte markera/avmarkera frågan som ' +
                                        'vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. ' +
                                        'Annars kan du kontakta supporten');
                            }
                        });
                };
            }
        };
    }]);
