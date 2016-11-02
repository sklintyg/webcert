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

function isDefined(value) {
    return value !== null && typeof value !== 'undefined';
}
function isEmpty(value) {
    return value === null || typeof value === 'undefined' || value === '';
}
function returnJoinedArrayOrNull(value) {
    return value !== null && value !== undefined ? value.join(', ') : null;
}
function valueOrNull(value) {
    return value !== null && value !== undefined ? value : null;
}


var DEFAULT_QUESTION = {
    intygsId: '',
    rubrik: 'Komplettering',
    skickatTidpunkt: '2016-07-13T17:23:00',
    skickatAv: 'FKASSA',
    amne: 'AVSTMN',
    meddelande: '',
    paminnelseMeddelandeId: '',
    svarPa: {
        meddelandeId: '',
        referensId: ''
    },
    kompletteringar: [
      {
        text: 'Detta är kompletteringstexten...',
        frageId: '1',
        instans: undefined
      }
    ],
    meddelandeNr: 1,
    patientPersonId: '',
    sistaDatumForSvar: ''
};

angular.module('rhsIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';


        $scope.selectedEnhet = '';
        $scope.q = DEFAULT_QUESTION;

        $scope.deleteAllArendenOnUnit = function() {
            if ($scope.selectedEnhet !== '' && window.confirm('Är du verkligen helt säker på att du vill radera alla ärenden på ' + $scope.selectedEnhet + ' ur databasen?')) {
                $http({
                    method: 'DELETE',
                    url: '/testability/arendetest/enhet/' + $scope.selectedEnhet
                }).then(function successCallback(response) {
                    $scope.raderingsResultat = response.data;
                });
            }
        };

        $scope.remove = function(index) {
            if (index == 0) {
                return;
            }
            $scope.q.kompletteringar.splice(index, 1);
        };

        $scope.add = function() {
            var kompl = {
                text: 'Detta är kompletteringstexten...',
                frageId: '1',
                instans: ''
            };

            $scope.q.kompletteringar.push(kompl);
        };

        $scope.loadIntyg = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/' + $scope.selectedEnhet
            }).then(function successCallback(response) {
                $scope.resultat = '';
                $scope.data = response.data;
            });
        };

        $scope.loadUnits = function() {
            $http({
                method: 'GET',
                url: '/testability/intyg/signingunits'
            }).then(function successCallback(response) {
                $scope.units = response.data;
            });
        };

        $scope.openForm = function(intyg) {
            $http({
                method: 'GET',
                url: '/testability/intyg/questions/' + intyg.intygsTyp
            }).then(function successCallback(response) {
                $scope.questions = response.data;
            });

            $http({
                method: 'GET',
                url: '/testability/arendetest/intyg/' + intyg.intygsId
            }).then(function successCallback(response) {
                $scope.pendingActionQuestions = response.data;
            });

            $scope.q.meddelandeId = guid();
            $scope.q.intygsId = intyg.intygsId;
            $scope.q.patientPersonId = intyg.patientPersonnummer.replace('-', '');
            $scope.q.enhetsId = intyg.enhetsId;
            $scope.q.svarPa = {};

            $scope.resultat = '';
        };

        $scope.typeClicked = function() {
            if ($scope.q.amne === 'KOMPLT') {
                $scope.q.rubrik = 'Komplettering';
            } else {
                $scope.q.rubrik = 'Fråga';
            }
        };

        $scope.sendQuestion = function(q) {

            var referensId = '';
            if (!isEmpty(q.referensId)) {
                referensId = '<urn1:referens-id>' + q.referensId + '</urn1:referens-id>';
            }

            var svarPa = '';
            if (!isEmpty(q.svarPa.meddelandeId)) {
                var svarPaReferensId = '';
                if (!isEmpty(q.svarPa.referensId)) {
                    svarPaReferensId = '<urn3:referens-id>' + q.svarPa.referensId + '</urn3:referens-id>';
                }
                svarPa = '<urn1:svarPa><urn3:meddelande-id>' + q.svarPa.meddelandeId + '</urn3:meddelande-id>' + svarPaReferensId + '</urn1:svarPa>';
            }
            var paminnelseMeddelandeId = '';
            if (!isEmpty(q.paminnelseMeddelandeId)) {
                paminnelseMeddelandeId = '<urn1:paminnelseMeddelande-id>' + q.paminnelseMeddelandeId + '</urn1:paminnelseMeddelande-id>';
            }
            var rubrik = '';
            if (!isEmpty(q.rubrik)) {
                rubrik = '<urn1:rubrik>' + q.rubrik + '</urn1:rubrik>';
            }
            var meddelande = '<urn1:meddelande>' + q.meddelande + '</urn1:meddelande>';
            var kompletteringsMarkup = '';
            if (q.amne === 'KOMPLT') {
                for(var a = 0; a < q.kompletteringar.length; a++) {
                    var kpl = q.kompletteringar[a];
                    var instansMarkup = '';
                    if (isDefined(kpl.instans) && kpl.instans > -1) {
                        instansMarkup = '<urn1:instans>' + kpl.instans + '</urn1:instans>';
                    }
                    kompletteringsMarkup += '<urn1:komplettering> \
                                    <urn1:frage-id>' + kpl.frageId + '</urn1:frage-id> \
                                    ' + instansMarkup + ' \
                                    <urn1:text>' + kpl.text + '</urn1:text> \
                                </urn1:komplettering>';
                }

            }
            var sistaDatumForSvar = '';
            if (!isEmpty(q.sistaDatumForSvar)) {
                sistaDatumForSvar = '<urn1:sistaDatumForSvar>' + q.sistaDatumForSvar + '</urn1:sistaDatumForSvar>';
            }

            var msg =
                '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:2" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:2"> \
                    <soapenv:Header> \
                        <urn:LogicalAddress>?</urn:LogicalAddress>  \
                    </soapenv:Header> \
                    <soapenv:Body> \
                     <urn1:SendMessageToCare> \
                        <urn1:meddelande-id>' + q.meddelandeId + '</urn1:meddelande-id> \
                        ' + referensId + '      \
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
                        ' + kompletteringsMarkup + ' \
                        ' + sistaDatumForSvar + ' \
                    </urn1:SendMessageToCare> \
                </soapenv:Body>  \
            </soapenv:Envelope>';

            $http({
                method: 'POST',
                url: '/services/send-message-to-care/v1.0',
                data: msg
            }).then(function successCallback(response) {

                if (response.status === 200) {
                    var startIdx = response.data.indexOf('result>');
                    var endIdx = response.data.substring(startIdx+7, response.data.length).indexOf('result>');
                    $scope.resultat = response.data.substring(startIdx+7, startIdx+7 + endIdx-2);
                    $scope.q.meddelandeId = guid();
                } else {
                    $scope.resultat = '"Servern svarade med HTTP ' + response.status + ' ' + response.statusText + '. Det betyder att någonting gick fel.';
                }

            });
        };

        $scope.loadUnits();

    }]);
