/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

describe(
    'normlaaEnhetsvalPageCtrlSpec',
    function() {
      'use strict';

      var $controller;
      var $scope;
      var $uibModal;
      var $window;
      var $windowSpy;
      var UserModel;
      var UserServiceMock;
      var $stateSpy;
      var $stateParamsMock;

      beforeEach(function() {

        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

          $provide.value('common.UserModel', {});
          $provide.value('$uibModal', {
            open: function() {
            }
          });

          $stateSpy = jasmine.createSpyObj('$state', ['go']);
          $provide.value('$state', $stateSpy);

          $stateParamsMock = {
            destinationState: {name: 'originalState'}
          };
          $provide.value('$stateParams', $stateParamsMock);

          UserServiceMock = jasmine.createSpyObj('common.User', ['setValdVardenhet']);
          $provide.value('common.User', UserServiceMock);

          $windowSpy = jasmine.createSpyObj('$window', ['location', 'location.href']);
          $provide.value('$window', $windowSpy);
        }]);

        inject(['$rootScope', '$window', '$uibModal', '$state', '$stateParams', 'common.UserModel', '$controller',
          function($rootScope, _$window_, _$uibModal_, _UserModel_, _$state_, _$stateParams_, _$controller_) {
            $scope = $rootScope.$new();
            $window = _$window_;
            UserModel = _UserModel_;
            $controller = _$controller_;
            $uibModal = _$uibModal_;

            spyOn($uibModal, 'open').and.returnValue({
              close: function() {
              }
            });
          }]);

      });

      describe('when trying to select a vardenhet', function() {

        beforeEach(function() {
          $controller('normal.EnhetsvalPageCtrl', {
            $scope: $scope
          });
        });

        it('should change state after successfully changing vardenhet', function() {

          UserServiceMock.setValdVardenhet.and.callFake(function(enhet, success, error) {
            success({user: {}});
          });

          $scope.onUnitSelected({
            id: '1234'
          });

          expect($uibModal.open).toHaveBeenCalled();
          expect(UserServiceMock.setValdVardenhet).toHaveBeenCalled();
          expect($stateSpy.go).toHaveBeenCalledWith('originalState', {}, {location: 'replace'});
        });

        it('should go to error page when failing to change vardenhet', function() {

          UserServiceMock.setValdVardenhet.and.callFake(function(enhet, success, error) {
            error({});
          });

          $scope.onUnitSelected({
            id: '1234'
          });

          expect($uibModal.open).toHaveBeenCalled();
          expect(UserServiceMock.setValdVardenhet).toHaveBeenCalled();
          expect($stateSpy.go).not.toHaveBeenCalled();
          expect($windowSpy.location.href).toBe('/error.jsp?reason=login.failed');
        });

      });

    });
