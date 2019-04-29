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
    ['$log', '$scope', '$stateParams', '$state', '$location', '$rootScope', '$q',
        'webcert.SokSkrivIntygViewstate', 'webcert.IntygTypeSelectorModel', 'common.PatientModel',
        'webcert.IntygProxy', 'webcert.UtkastProxy', 'webcert.SokSkrivValjUtkastService', 'common.ObjectHelper',
        'common.UtkastProxy', 'common.authorityService', 'common.UserModel', 'common.moduleService', 'common.User',
        function($log, $scope, $stateParams, $state, $location, $rootScope, $q,
            Viewstate, IntygTypeSelectorModel, PatientModel,
            IntygProxy, UtkastProxy, Service, ObjectHelper,
            commonUtkastProxy, authorityService, UserModel, moduleService, UserService) {
            'use strict';

            var favouriteList,
                intygTypeModel,
                choosePatientStateName = 'webcert.create-choosepatient-index';

            /**
             * Page state
             */

            $scope.viewState = Viewstate.build();
            intygTypeModel = IntygTypeSelectorModel.build();
            $scope.viewState.IntygTypeSelectorModel = intygTypeModel;

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

                loadFavouriteList();
            }

            function loadFavouriteList() {
                favouriteList = UserModel.getAnvandarPreference('wc.favoritIntyg');
                if(favouriteList) {
                    try {
                        favouriteList = JSON.parse(favouriteList);
                    } catch (e) {
                        if (e instanceof SyntaxError) {
                            favouriteList = [];
                        }
                    }
                } else {
                    favouriteList = [];
                }
            }


            function isFavourite(type) {
                return favouriteList.indexOf(type.id) > -1;
            }


            function loadUtkastTypesAndIntyg() {

                // Also load global set of modules
                intygTypeModel.intygTypes = UtkastProxy.getUtkastTypes();

                // Load intyg types user can choose from
                UtkastProxy.getUtkastTypesForPatient(PatientModel.personnummer, function(types) {
                    intygTypeModel.userIntygTypes = types;

                    intygTypeModel.userIntygTypes.forEach(function(intygTypeData){
                        var intygsModule = moduleService.getModule(intygTypeData.id);
                        intygTypeData.issuerTypeId = intygsModule.issuerTypeId;
                    });
                });

                // set favourite flag
                intygTypeModel.intygTypes.forEach(function(type) {
                    type.isFavourite = isFavourite(type);
                });

                // load warnings of previous certificates
                commonUtkastProxy.getPrevious(PatientModel.personnummer, function(existing) {
                    intygTypeModel.previousIntygWarnings = existing.intyg;
                    intygTypeModel.previousUtkastWarnings = existing.utkast;
                });

                // Load intyg for person with specified pnr
                Viewstate.tidigareIntygLoading = true;
                IntygProxy.getIntygForPatient(PatientModel.personnummer, function(data) {
                    Viewstate.intygListUnhandled = data;
                    Viewstate.intygListUnhandled.forEach(function(intyg) {
                        intyg.intygTypeName = getTypeName(intyg.intygType);
                        setExtendedStatus(intyg);
                    });
                    Service.updateIntygList(Viewstate);
                    Viewstate.unsigned = Service.hasUnsigned(Viewstate.currentList);
                    Viewstate.tidigareIntygLoading = false;
                }, function(errorCode, errorData) {
                    Viewstate.tidigareIntygLoading = false;
                    $log.debug('Query Error. Code: ' + errorCode + '. Data:[' + errorData + ']');
                    Viewstate.intygListErrorMessageKey = errorData;
                });

            }

            function setExtendedStatus(intyg) {
                intyg.extendedStatus = null;

                // Handle makulerat
                if (intyg.status === 'CANCELLED') {
                    intyg.extendedStatus = intyg.status;
                } else {
                    // These 2 scenarios overrides actual status of the intyg.
                    // Maybe we shoould utilize this to remove similar
                    // ersatt/kompletterat checks in tidigareIntyg.filter.js
                    if (isKompletterad(intyg)) {
                        intyg.extendedStatus = 'KOMPLETTERAT_AV_INTYG';
                    } else if (isErsatt(intyg)) {
                        intyg.extendedStatus = 'ERSATT_AV_INTYG';
                    }

                    if (intyg.extendedStatus === null) {
                        intyg.extendedStatus = intyg.status;
                    }
                }
            }

            function isErsatt(intyg) {
                if (typeof intyg.relations !== 'undefined' &&
                    typeof intyg.relations.latestChildRelations !== 'undefined' &&
                    typeof intyg.relations.latestChildRelations.replacedByIntyg !== 'undefined') {
                    return true;
                }
                return false;
            }

            function isKompletterad(intyg) {
                if (typeof intyg.relations !== 'undefined' &&
                    typeof intyg.relations.latestChildRelations !== 'undefined' &&
                    typeof intyg.relations.latestChildRelations.complementedByIntyg !== 'undefined' &&
                    !intyg.relations.latestChildRelations.complementedByIntyg.makulerat) {
                    return true;
                }
                return false;
            }

            //Use loaded module metadata to look up name for a intygsType
            function getTypeName (intygsType) {
                var intygTypes = intygTypeModel.intygTypes.filter(function (intygType) {
                    return (intygType.id === intygsType);
                });
                if (intygTypes && intygTypes.length > 0) {
                    return intygTypes[0].label;
                }
            }

            $scope.toggleFavourite = function(type) {
                type.isFavourite = !isFavourite(type);

                if(type.isFavourite) {
                    favouriteList.push(type.id);
                } else {
                    // remove favourite
                    favouriteList.splice(favouriteList.indexOf(type.id), 1);
                }

                // store favourites
                UserService.storeAnvandarPreference('wc.favoritIntyg', JSON.stringify(favouriteList));
            };

            /**
             * Watches
             */

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
