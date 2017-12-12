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

angular.module('webcert').directive('wcEnhetArendenList', [
    '$location', '$log', '$timeout', '$window',
    'common.ArendeVidarebefordraHelper', 'common.ArendeProxy', 'common.dialogService',
    'webcert.enhetArendenListService', 'webcert.enhetArendenModel', 'webcert.enhetArendenListModel',
    function($location, $log, $timeout, $window,
        ArendeVidarebefordraHelper, ArendeProxy, dialogService,
        enhetArendenListService, enhetArendenModel, enhetArendenListModel) {
        'use strict';

        return {
            restrict: 'E',
            transclude: false,
            replace: false,
            scope: {},
            templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenList/wcEnhetArendenList.directive.html',
            controller: function($scope) {

                $scope.listModel = enhetArendenListModel;
                updateArenden(null, {startFrom: 0});

                // When other directives want to request list update
                function updateArenden(event, data){
                    enhetArendenListModel.viewState.runningQuery = true;
                    enhetArendenListModel.viewState.activeErrorMessageKey = null;
                    enhetArendenListService.getArenden(data.startFrom).then(function(arendenListResult){

                        enhetArendenListModel.prevFilterQuery = arendenListResult.query;
                        enhetArendenListModel.totalCount = arendenListResult.totalCount;
                        enhetArendenListModel.arendenList = arendenListResult.arendenList;

                        enhetArendenListModel.viewState.runningQuery = false;

                    }, function(errorData){
                        $log.debug('Query Error: ' + errorData);
                        enhetArendenListModel.viewState.runningQuery = false;
                        enhetArendenListModel.viewState.activeErrorMessageKey = 'info.query.error';
                    });
                }
                $scope.$on('enhetArendenList.requestListUpdate', updateArenden);

                $scope.fetchMore = function() {
                    enhetArendenListModel.viewState.fetchingMoreInProgress = true;
                    enhetArendenListModel.viewState.activeErrorMessageKey = null;
                    enhetArendenListService.getArenden(enhetArendenListModel.prevFilterQuery.startFrom + enhetArendenModel.PAGE_SIZE).then(function(arendenListResult){

                        // Add fetch more result to existing list
                        enhetArendenListModel.prevFilterQuery = arendenListResult.query;
                        enhetArendenListModel.totalCount = arendenListResult.totalCount;
                        var arendenList = enhetArendenListModel.arendenList;
                        for (var i = 0; i < arendenListResult.arendenList.length; i++) {
                            arendenList.push(arendenListResult.arendenList[i]);
                        }
                        enhetArendenListModel.fetchingMoreInProgress = false;

                    }, function(errorData){
                        $log.debug('Query Error: ' + errorData);
                        enhetArendenListModel.fetchingMoreInProgress = false;
                        enhetArendenListModel.viewState.activeErrorMessageKey = 'info.query.error';
                    });
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
