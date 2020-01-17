/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
  'common.messageService', 'webcert.vardenhetFilterModel', 'webcert.enhetArendenFilterModel',
  'common.UserModel', 'common.IntygProxy', 'common.ResourceLinkService',
  function($location, $log, $timeout, $window,
      ArendeVidarebefordraHelper, ArendeProxy, dialogService,
      enhetArendenListService, enhetArendenModel, enhetArendenListModel, messageService,
      vardenhetFilterModel, enhetArendenFilterModel, UserModel, IntygProxy, ResourceLinkService) {
    'use strict';

    return {
      restrict: 'E',
      transclude: false,
      replace: false,
      scope: {},
      templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenList/wcEnhetArendenList.directive.html',
      controller: function($scope, $rootScope) {
        $scope.listModel = enhetArendenListModel;
        $scope.filterModel = enhetArendenFilterModel;
        $scope.vardenhetFilterModel = vardenhetFilterModel;
        $scope.selectedUnitName = vardenhetFilterModel.selectedUnitName;

        $scope.forwardTooltip = messageService.getProperty('th.help.forward');
        $scope.openTooltip = messageService.getProperty('th.help.open');

        $scope.orderBy = enhetArendenFilterModel.filterForm.orderBy;
        $scope.orderAscending = enhetArendenFilterModel.filterForm.orderAscending;

        var vidarebefordraArendeMailModel = null;

        if (!UserModel.isDjupintegration() && !UserModel.isVardAdministrator()) {
          enhetArendenFilterModel.filterForm.lakareSelector = UserModel.user.hsaId;
        } else {
          enhetArendenFilterModel.filterForm.lakareSelector = enhetArendenFilterModel.lakareList[0].id;
        }

        $scope.listInit = function() {
          $scope.listModel.limit = $scope.listModel.DEFAULT_PAGE_SIZE;
          $scope.filterModel.pageSize = $scope.listModel.limit;
          $scope.listModel.chosenPage = $scope.listModel.DEFAULT_PAGE;
          $scope.listModel.chosenPageList = $scope.listModel.DEFAULT_PAGE;
        };

        updateArenden(null, {startFrom: 0}, true);

        // When other directives want to request list update
        function updateArenden(event, data, firstRun) {
          var spinnerWaiting = $timeout(function() {
            enhetArendenListModel.viewState.runningQuery = true;
          }, 700);

          enhetArendenListModel.viewState.activeErrorMessageKey = null;

          if(data.reset) {
            $scope.orderBy = 'receivedDate';
            $scope.orderAscending = false;
            $scope.listInit();
          }

          enhetArendenListService.getArenden(data.startFrom).then(function(arendenListResult) {
            enhetArendenListModel.prevFilterQuery = arendenListResult.query;
            enhetArendenListModel.totalCount = arendenListResult.totalCount;
            enhetArendenListModel.arendenList = arendenListResult.arendenList;

            if (vardenhetFilterModel.selectedUnitName) {
              $scope.selectedUnitName = vardenhetFilterModel.selectedUnitName;
            }

            if (firstRun) {
              $scope.totalCount = arendenListResult.totalCount;
            }
          }, function(errorData) {
            $log.debug('Query Error: ' + errorData);
            enhetArendenListModel.viewState.activeErrorMessageKey = 'info.query.error';
          }).finally(function() {  // jshint ignore:line
            if (spinnerWaiting) {
              $timeout.cancel(spinnerWaiting);
            }
            enhetArendenListModel.viewState.runningQuery = false;

            if ($scope.totalCount === undefined || $scope.totalCount === 0) {
              enhetArendenFilterModel.filterForm.lakareSelector = undefined;
              enhetArendenListService.getArenden(data.startFrom).then(function(arendenListResult) {
                if (firstRun) {
                  $scope.totalCount = arendenListResult.totalCount;
                }
              }, function(errorData) {
                $log.debug('Query Error: ' + errorData);
                enhetArendenListModel.viewState.activeErrorMessageKey = 'info.query.error';
              }).finally(function() {  // jshint ignore:line
                if (spinnerWaiting) {
                  $timeout.cancel(spinnerWaiting);
                }
                enhetArendenListModel.viewState.runningQuery = false;
              });
            }

            $rootScope.$broadcast('wcLimitDropdown.getLimits');
            $rootScope.$broadcast('wcListPageNumbers.getPages');

            $scope.listModel.startPoint = data.startFrom + 1;
            $scope.listModel.endPoint = data.startFrom + $scope.listModel.arendenList.length;
          });
        }

        function setVidarebefordradStateInView(intygId) {
          for (var i in $scope.listModel.arendenList) {
            if ($scope.listModel.arendenList[i].intygId === intygId) {
              $scope.listModel.arendenList[i].vidarebefordrad = true;
            }
          }
        }

        $scope.$on($scope.listModel.LIST_NAME + '.requestListUpdate', updateArenden);

        $scope.showVidarebefodra = function(arende) {
          return ResourceLinkService.isLinkTypeExists(arende.links, 'VIDAREBEFODRA_FRAGA');
        };

        $scope.openIntyg = function(intygId, intygTyp) {
          $log.debug('open intyg ' + intygId + ' of type ' + intygTyp);
          IntygProxy.getIntygTypeInfo(intygId, function success(typeInfo) {
            $location.url(
                '/fragasvar/' + intygTyp.toLowerCase() + '/' + typeInfo.intygTypeVersion + '/' + intygId);
          }, function fail() {
            enhetArendenListModel.viewState.activeErrorMessageKey = 'info.query.error';
          });

        };

        // Handle vidarebefordra dialog
        $scope.openMailDialog = function(arende) {
          $timeout(function() {
            ArendeVidarebefordraHelper.handleVidareBefordradToggle($scope.onVidareBefordradChange);
          }, 1000);

          // Launch mail client
          vidarebefordraArendeMailModel = {
            intygId: arende.intygId,
            intygType: arende.intygTyp
          };
          $window.location = ArendeVidarebefordraHelper.buildMailToLink(vidarebefordraArendeMailModel);
        };

        $scope.onVidareBefordradChange = function() {
          ArendeProxy.setVidarebefordradState(
              vidarebefordraArendeMailModel.intygId,
              vidarebefordraArendeMailModel.intygType,
              function(result) {
                if (result) {
                  setVidarebefordradStateInView(vidarebefordraArendeMailModel.intygId);
                } else {
                  dialogService
                  .showErrorMessageDialog('Kunde inte markera/avmarkera frågan som ' +
                      'vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. ' +
                      'Annars kan du kontakta supporten');
                }
              });
        };

        $scope.orderByProperty = function(property) {
          if (enhetArendenFilterModel.filterForm.orderBy === property) {
            enhetArendenFilterModel.filterForm.orderAscending =
                !enhetArendenFilterModel.filterForm.orderAscending;
          } else if(property === 'receivedDate') {
            enhetArendenFilterModel.filterForm.orderAscending = false;
          } else {
            enhetArendenFilterModel.filterForm.orderAscending = true;
          }
          enhetArendenFilterModel.filterForm.orderBy = property;
          $scope.orderBy = enhetArendenFilterModel.filterForm.orderBy;
          $scope.orderAscending = enhetArendenFilterModel.filterForm.orderAscending;

          updateArenden(null, {startFrom: $scope.listModel.startPoint - 1});
        };

        $scope.listInit();
      }
    };
  }]);
