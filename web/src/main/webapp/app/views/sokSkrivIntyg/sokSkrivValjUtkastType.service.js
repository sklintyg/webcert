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
angular.module('webcert').factory('webcert.SokSkrivValjUtkastService',
    [ '$q', 'common.PersonIdValidatorService', 'common.PatientProxy', 'common.ObjectHelper',
        function($q, PersonIdValidator, PatientProxy, ObjectHelper) {
            'use strict';

            function _setupPatientModel(PatientModel, patientIdParam){
                if(patientIdParam === 'default'){
                    // if param is 'default' we won't use it, instead try to rely on already stored id in PatientModel
                    patientIdParam = null;
                } else {
                    // if param is a valid personnummer use that
                    if(PersonIdValidator.validResult(PersonIdValidator.validate(patientIdParam))) {
                        PatientModel.personnummer = patientIdParam;
                    } else {
                        patientIdParam = null;
                    }
                }

                if(!PatientModel.isValid()){
                    var patientModel = PatientModel.build();
                    if(!ObjectHelper.isEmpty(patientIdParam)){
                        PatientModel.personnummer = patientIdParam;
                    }
                    return patientModel;
                } else {
                    return PatientModel;
                }
            }

            function _lookupPatient(personnummer) {

                var deferred = $q.defer();

                var onSuccess = function(patientResult) {

                    if (!patientResult.personnummer) {
                        // This shouldn't ever happen but in case we don't receive a personnummer we should tell the user.
                        deferred.reject('error.pu.nopersonnummer');
                    } else if (!patientResult.fornamn || !patientResult.efternamn) {
                        // If the successful result does not contain mandatory name information, present an error (should never happen in production)
                        deferred.reject('error.pu.noname');
                    } else {
                        deferred.resolve(patientResult);
                    }
                };

                var onNotFound = function() {

                    // If the pu-service says noone exists with this pnr we just show an error message.
                    if (PersonIdValidator.validResult(
                            PersonIdValidator.validateSamordningsnummer(personnummer))) {
                        // This is a samordningsnummer that does not exist
                        deferred.reject('error.pu.samordningsnummernotfound');
                    } else {
                        // This is a personnummer that does not exist
                        deferred.reject('error.pu.namenotfound');
                    }
                };

                var onError = function(isPUError) {
                    if (isPUError) {
                        deferred.reject('error.pu_problem');
                    } else {
                        deferred.reject('error.pu.server-error');
                    }

                };

                PatientProxy.getPatient(personnummer, onSuccess, onNotFound, onError);

                return deferred.promise;
            }

            function _hasUnsigned(list) {
                if (!list) {
                    return null;
                }
                if (list.length === 0) {
                    return 'intyglist-empty';
                }
                var unsigned = true;
                for (var i = 0; i < list.length; i++) {
                    var item = list[i];
                    if (item.status === 'DRAFT_COMPLETE') {
                        unsigned = false;
                        break;
                    }
                }
                if (unsigned) {
                    return 'unsigned';
                } else {
                    return 'signed';
                }
            }

            // Return public API for the service
            return {
                setupPatientModel: _setupPatientModel,
                lookupPatient: _lookupPatient,
                hasUnsigned: _hasUnsigned
            };
        }]);
