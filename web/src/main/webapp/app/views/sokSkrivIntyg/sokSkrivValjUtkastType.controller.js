/*
 * Copyright(C) 2016 Inera AB(http://www.inera.se)
 *
 * This file is part of sklintyg(https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
angular.module('webcert').controller('webcert.SokSkrivValjUtkastTypeCtrl',
    ['$log', '$scope', '$stateParams', '$state', '$location',
        'webcert.SokSkrivIntygViewstate', 'webcert.IntygTypeSelectorModel', 'common.PatientModel',
        'webcert.IntygProxy', 'webcert.UtkastProxy', 'webcert.SokSkrivValjUtkastService', 'common.ObjectHelper',
        'common.UtkastProxy', 'common.authorityService', 'common.UserModel', 'common.moduleService',

        function($log, $scope, $stateParams, $state, $location,
            Viewstate, IntygTypeSelectorModel, PatientModel,
            IntygProxy, UtkastProxy, Service, ObjectHelper,
            commonUtkastProxy, authorityService, UserModel, moduleService) {
            'use strict';

            /**
             * Page state
             */

            var choosePatientStateName = 'webcert.create-choosepatient-index';

            $scope.viewState = Viewstate.build();

            // In case callers do not know the patientId they can use 'default' in which case the controller
            // will use what's currently in PatientModel, or, if that's not available, redirect user to enter a
            // new id on the choose patient screen.
            $scope.patientModel = Service.setupPatientModel(PatientModel, $stateParams.patientId);
            onPageLoad();

            /**
             * Private functions
             * @private
             */

            function onPageLoad() {

                //Without personnr context - we have nothing to look up - transition to step 1.
                if (ObjectHelper.isEmpty(PatientModel.personnummer)) {
                    $state.go('webcert.create-choosepatient-index');
                    return;
                }

                // We should always validate skretesstatus from PU, as it's not certain we got here from the enter patient personnr search state
                Viewstate.patientLoading = true;
                Service.lookupPatient(PatientModel.personnummer).then(function(patientResult) {

                    Viewstate.loadErrorMessageKey = null;
                    Viewstate.patientLoading = false;

                    // Redirect to index if pnr and name still isn't specified
                    if (!PatientModel.update(patientResult)) {
                        $state.go(choosePatientStateName);
                        return;
                    }

                    //Patient is now verified via PU lookup - continue loading intyg and utkasttypes.
                    loadUtkastTypesAndIntyg();

                }, function(errorId) {
                    Viewstate.loadErrorMessageKey = errorId;
                    Viewstate.patientLoading = false;
                });
            }

            function loadUtkastTypesAndIntyg() {
                // Load intyg types user can choose from
                UtkastProxy.getUtkastTypesForPatient(PatientModel.personnummer, function(types) {
                    IntygTypeSelectorModel.userIntygTypes = types;

                    IntygTypeSelectorModel.userIntygTypes.forEach(function(intygTypeData){
                        var intygsModule = moduleService.getModule(intygTypeData.id);
                        intygTypeData.issuerTypeId = intygsModule.issuerTypeId;
                    });
                });

                // Also load global set of modules
                UtkastProxy.getUtkastTypes(function(types) {
                    IntygTypeSelectorModel.intygTypes = types;
                });

                // load warnings of previous certificates
                commonUtkastProxy.getPrevious(PatientModel.personnummer, function(existing) {
                    IntygTypeSelectorModel.previousIntygWarnings = existing.intyg;
                    IntygTypeSelectorModel.previousUtkastWarnings = existing.utkast;
                });

                // Load intyg for person with specified pnr
                Viewstate.tidigareIntygLoading = true;
                IntygProxy.getIntygForPatient(PatientModel.personnummer, function(data) {
                    Viewstate.intygListUnhandled = data;
                    Service.updateIntygList(Viewstate);
                    Viewstate.unsigned = Service.hasUnsigned(Viewstate.currentList);
                    Viewstate.tidigareIntygLoading = false;
                }, function(errorData, errorCode) {
                    Viewstate.tidigareIntygLoading = false;
                    $log.debug('Query Error' + errorData);
                    Viewstate.intygListErrorMessageKey = errorCode;
                });
            }

            /**
             * Watches
             */

            //Use loaded module metadata to look up name for a intygsType
            $scope.getTypeName = function(intygsType) {
                var intygTypes = IntygTypeSelectorModel.intygTypes.filter(function(intygType) {
                    return (intygType.id === intygsType);
                });
                if (intygTypes && intygTypes.length > 0) {
                    return intygTypes[0].label;
                }
            };

            $scope.hasPrivilege = function () {
                if (PatientModel.sekretessmarkering) {
                    var canHandleSecrecy = authorityService.isAuthorityActive({
                        authority: UserModel.privileges.HANTERA_SEKRETESSMARKERAD_PATIENT
                    });
                    return canHandleSecrecy;
                } 
                return true;

            };
        }]);
