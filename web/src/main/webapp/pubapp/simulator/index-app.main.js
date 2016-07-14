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

//Register module
angular.module('rhsIndexApp', [

]);

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}

var DEFAULT_QUESTION = {
    intygsId: '',
    rubrik: 'Komplettering',
    skickatTidpunkt: '2016-07-13T17:23:00',
    skickatAv: 'FKASSA',
    amne: 'KOMPLT',
    meddelande: '',
    paminnelseMeddelandeId: '',
    svarPa: {
        meddelandeId: '',
        referensId: ''
    },
    komplettering: {
        text: 'Detta är kompletteringstexten...',
        frageId: '1',
        instans: ''
    },
    meddelandeNr: 1,
    patientPersonId: ''
};

angular.module('rhsIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';


        $scope.q = DEFAULT_QUESTION;

        $scope.deleteAllArenden = function() {
            if (window.confirm('Är du verkligen helt säker på att du vill radera alla Ärenden ur databasen?')) {
                $http({
                    method: 'DELETE',
                    url: '/testability/arendetest'
                }).then(function successCallback(response) {
                    $scope.raderingsResultat = response.data;
                });
            }
        };

        $scope.loadIntyg = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/' + $scope.selectedEnhet
            }).then(function successCallback(response) {
                $scope.data = response.data;
            })
        };

        $scope.loadDoctors = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/signingunits'
            }).then(function successCallback(response) {
                $scope.doctors = response.data;
            })
        };

        $scope.openForm = function(intyg) {
            $http({
                method: 'GET',
                url: '/testability/intyg/questions/' + intyg.intygsTyp
            }).then(function successCallback(response) {
                $scope.questions = response.data;
            });

            $scope.q.meddelandeId = guid();
            $scope.q.intygsId = intyg.intygsId;
            $scope.q.patientPersonId = intyg.patientPersonnummer.replace('-', '');
            $scope.q.enhetsId = intyg.enhetsId;
        };

        $scope.typeClicked = function() {
            if ($scope.q.amne == 'KOMPLT') {
                $scope.q.rubrik = 'Komplettering';
            } else {
                $scope.q.rubrik = 'Fråga';
            }
        };

        $scope.sendQuestion = function(q) {

            var svarPa = '';
            if (q.svarPa.meddelandeId != '') {
                svarPa = '<urn1:svarPa><urn3:meddelande-id>' + q.svarPa.meddelandeId + '</urn3:meddelande-id><urn3:referens-id>' + q.svarPa.referensId + '</urn3:referens-id></urn1:svarPa>';
            }
            var paminnelseMeddelandeId = '';
            if (q.paminnelseMeddelandeId != '') {
                paminnelseMeddelandeId = '<urn1:paminnelseMeddelande-id>' + q.pamminelseMeddelandeId + '</urn1:paminnelseMeddelande-id>';
            }
            var rubrik = '';
            if (q.rubrik != '') {
                rubrik = '<urn1:rubrik>' + q.rubrik + '</urn1:rubrik>';
            }
            var meddelande = '<urn1:meddelande>' + q.meddelande + '</urn1:meddelande>';
            var komplettering = '';
            if (q.amne == 'KOMPLT') {
                komplettering = '<urn1:komplettering> \
                                <urn1:frage-id>' + q.komplettering.frageId + '</urn1:frage-id> \
                                <urn1:text>' + q.komplettering.text + '</urn1:text> \
                                </urn1:komplettering>';
            }

            var msg =
                '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:2" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:2"> \
                    <soapenv:Header> \
                        <urn:LogicalAddress>?</urn:LogicalAddress>  \
                    </soapenv:Header> \
                    <soapenv:Body> \
                     <urn1:SendMessageToCare> \
                        <urn1:meddelande-id>' + q.meddelandeId + '</urn1:meddelande-id> \
                        <urn1:referens-id>' + q.referensId + '</urn1:referens-id>      \
                        <urn1:skickatTidpunkt>' + q.skickatTidpunkt + '</urn1:skickatTidpunkt>   \
                        <urn1:intygs-id>   \
                            <urn2:root></urn2:root>  \
                            <urn2:extension>' + q.intygsId + '</urn2:extension> \
                        </urn1:intygs-id> \
                        <urn1:patientPerson-id> \
                            <urn2:root>1.2.752.129.2.1.3.1</urn2:root>  \
                            <urn2:extension>' + q.patientPersonId + '</urn2:extension> \
                        </urn1:patientPerson-id>  \
                        <urn1:logiskAdressMottagare>' + q.enhetsId + '</urn1:logiskAdressMottagare> \
                        <urn1:amne> \
                            <urn2:code>' + q.amne + '</urn2:code> \
                            <urn2:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</urn2:codeSystem> \
                        </urn1:amne> \
                        ' + rubrik + ' \
                        ' + meddelande + ' \
                        ' + paminnelseMeddelandeId + ' \
                        ' + svarPa + ' \
                        <urn1:skickatAv> \
                            <urn1:part>  \
                              <urn2:code>' + q.skickatAv + '</urn2:code>         \
                              <urn2:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</urn2:codeSystem>  \
                            </urn1:part> \
                        </urn1:skickatAv> \
                        ' + komplettering + ' \
                    </urn1:SendMessageToCare> \
                </soapenv:Body>  \
            </soapenv:Envelope>';

            $http({
                method: 'POST',
                url: '/services/send-message-to-care/v1.0',
                data: msg
            }).then(function successCallback(response) {

                if (response.status == 200) {
                    var startIdx = response.data.indexOf('result>');
                    var endIdx = response.data.substring(startIdx+7, response.data.length).indexOf('result>');
                    $scope.resultat = response.data.substring(startIdx+7, startIdx+7 + endIdx-2);
                    $scope.q.meddelandeId = guid();
                } else {
                    $scope.resultat = "Servern svarade med HTTP " + response.status + " " + response.statusText + ". Det betyder att någonting gick fel.";
                }

            });
        };

        $scope.loadDoctors();

    }]);
