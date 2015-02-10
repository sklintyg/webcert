angular.module('webcert').factory('webcert.CreateCertificateDraft',
    [ '$http', '$log', 'common.statService',
        function($http, $log, statService) {
            'use strict';

            return {
                reset: function() {
                    this.personnummer = null;
                    this.intygId = null;
                    this.intygType = 'default';
                    this.fornamn = null;
                    this.mellannamn = null;
                    this.efternamn = null;
                    this.postadress = null;
                    this.postnummer = null;
                    this.postort = null;
                },

                getNameAndAddress: function(personnummer, onSuccess, onNotFound, onError) {
                    $log.debug('CreateCertificateDraft getNameAndAddress');

                    var that = this;
                    that.personnummer = personnummer;

                    var restPath = '/api/person/' + personnummer;
                    $http.get(restPath).success(function(data) {
                        $log.debug(data);

                        if (data.status === 'FOUND' && data.person) {
                            that.fornamn = data.person.fornamn;
                            that.mellannamn = data.person.mellannamn;
                            that.efternamn = data.person.efternamn;
                            that.postadress = data.person.postadress;
                            that.postnummer = data.person.postnummer;
                            that.postort = data.person.postort;
                            onSuccess();
                        } else if (data.status === 'ERROR') {
                            $log.warn('PU-tjänsten kunde inte kontaktas, manuell inmatning krävs');
                            onError();
                        } else {
                            $log.debug('Personen hittades inte i PU-tjänsten, manuell inmatning krävs');
                            onNotFound();
                        }

                    }).error(function() {
                        $log.warn('PU-tjänsten kunde inte kontaktas, manuell inmatning krävs');
                        onError();
                    });
                },

                createDraft: function(createDraftRequestPayload, onSuccess, onError) {
                    $log.debug('_createDraft');
                    var restPath = '/api/utkast/' + createDraftRequestPayload.intygType;
                    $http.post(restPath, createDraftRequestPayload).success(function(data) {
                        $log.debug('got callback data: ' + data);
                        onSuccess(data);
                        statService.refreshStat();
                    }).error(function(data, status) {
                        $log.error('error ' + status);
                        onError(data);
                    });
                },

                copyIntygToDraft: function(intygCopyRequest, onSuccess, onError) {
                    $log.debug('_copyIntygToDraft ' + intygCopyRequest.intygType + ', ' + intygCopyRequest.intygId);

                    var payload = {};
                    payload.patientPersonnummer = intygCopyRequest.patientPersonnummer;
                    if (this.nyttPatientPersonnummer) {
                        payload.nyttPatientPersonnummer = intygCopyRequest.nyttPatientPersonnummer;
                    }

                    var restPath = '/api/intyg/' + intygCopyRequest.intygType + '/' + intygCopyRequest.intygId + '/kopiera/';
                    $http.post(restPath, payload).success(function(data) {
                        $log.debug('got callback data: ' + data);
                        onSuccess(data);
                        statService.refreshStat();

                    }).error(function(data, status) {
                        $log.error('error ' + status);
                        onError(data);
                    });
                }
            };
        }]);
