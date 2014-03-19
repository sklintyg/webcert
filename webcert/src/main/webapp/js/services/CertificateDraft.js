define([
], function () {
    'use strict';

    return ['$http', '$log', function ($http, $log) {
        return {

            reset : function () {
                this.personnummer = null;
                this.intygType = 'default';
                this.firstname = null;
                this.lastname = null;
                this.address = null;
                this.vardEnhetHsaId = null;
                this.vardEnhetNamn = null;
                this.vardGivareHsaId = null;
                this.vardGivareHsaNamn = null;
            },

            getNameAndAddress : function (personnummer, onSuccess) {
                $log.debug('CertificateDraft getNameAndAddress');

                this.personnummer = personnummer;

                if (this.personnummer === '19121212-1212' || this.personnummer === '20121212-1212') {
                    this.firstname = 'Test';
                    this.lastname = 'Testsson';
                    this.address = 'Storgatan 23';
                } else {
                    this.firstname = null;
                    this.lastname = null;
                    this.address = null;
                }
                onSuccess();
            },

            getCertTypes : function (onSuccess, onError) {
                this.intygType = 'default';

                var restPath = '/api/modules/map';
                $http.get(restPath).success(function (data) {
                    $log.debug('got data:', data);
                    var sortValue = 0;
                    var types = [
                        {sortValue : sortValue++, id : 'default', label : 'Välj intygstyp'}
                    ];
                    for (var i in data) {
                        var m = data[i];
                        types.push({sortValue : sortValue++, id : m.id, label : m.label})
                    }
                    onSuccess(types);
                }).error(function (data, status) {
                    $log.error('error ' + status);
                    onError();
                });
            },

            createDraft : function (onSuccess, onError) {
                $log.debug('_createDraft');

                var payload = {};
                payload.intygType = this.intygType;

                payload.patient = {};
                payload.patient.personNummer = this.personnummer;
                payload.patient.forNamn = this.firstname;
                payload.patient.efterNamn = this.lastname;
                payload.patient.postAdress = this.address;
                payload.patient.postNummer = '12345';
                payload.patient.postort = 'Göteborg';

                payload.vardenhet = {};
                payload.vardenhet.hsaId = this.vardEnhetHsaId;
                payload.vardenhet.namn = this.vardEnhetNamn;
                payload.vardenhet.postaddress = 'Storgatan 1';
                payload.vardenhet.postnummer = '12345';
                payload.vardenhet.postort = 'Göteborg';
                payload.vardenhet.telefonnummer = '031-123456';

                payload.vardenhet.vardgivare = {};
                payload.vardenhet.vardgivare.hsaId = this.vardGivareHsaId;
                payload.vardenhet.vardgivare.namn = this.vardGivareNamn;

                var restPath = '/api/intyg/create';
                $http.post(restPath, payload).success(function (data) {
                    $log.debug('got callback data: ' + data);
                    onSuccess(data);

                }).error(function (data, status) {
                    $log.error('error ' + status);
                    onError(data);
                });
            }
        };
    }];
});
