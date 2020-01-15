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

angular.module('webcert').directive('wcValjUtkastTyp',
    ['$log', '$location', 'webcert.SokSkrivIntygViewstate', 'common.messageService', 'common.featureService',
      'common.PatientModel', 'webcert.UtkastProxy', 'common.dialogService', 'common.dynamicLabelService',
      'common.ResourceLinkService',
      function($log, $location, ViewState, messageService, featureService, PatientModel, UtkastProxy, DialogService,
          dynamicLabelService, ResourceLinkService) {
        'use strict';

        return {
          restrict: 'E',
          scope: {
            viewState: '=',
            domId: '@',
            favouriteCallback: '&'
          },
          templateUrl: '/app/views/sokSkrivIntyg/valjUtkastTyp.directive.html',
          link: function(scope, element, attrs) {

            var IntygTypeSelectorModel = scope.viewState.IntygTypeSelectorModel;
            scope.intygTypeModel = IntygTypeSelectorModel;
            scope.intygReplacement = {
              'fk7263': 'lisjp'
            };

            scope.getIntygTypeList = function() {
              return IntygTypeSelectorModel.intygTypes.filter(function(intygType) {
                return !intygType.deprecated || intygType.displayDeprecated;
              });
            };

            scope.openIntygTypeDetailsDialog = function(intygTypeData) {

              DialogService.showMessageDialogText(
                  'intyg-detail-description-intygType',
                  'Om ' + intygTypeData.label,
                  scope.getDetailedDescription(intygTypeData.id));

            };

            scope.checkType = function(intygType) {

              //Om utkastet är av typ DOI så ska en informationsdialog visas om ett Dödsbevis inte finns eller inte är skickat
              if (intygType === 'doi') {
                var intygList = ViewState.intygListUnhandled;
                var dbExists = false;

                for (var i = 0; i < intygList.length; i++) {
                  if (intygList[i].intygType === 'db' && intygList[i].status === 'SENT') {
                    dbExists = true;
                  }

                }
                if (!dbExists) {
                  DialogService.showDialog({
                    dialogId: 'doi-info-dialog',
                    titleText: 'doi.label.titleText',
                    bodyText: 'doi.label.bodyText',
                    templateUrl: '/app/partials/doiInfo.dialog.html',

                    button1click: function(modalInstance) {
                      scope.createDraft(intygType);
                      modalInstance.close();
                    },
                    button2click: function(modalInstance) {
                      modalInstance.close();
                    },
                    button1text: 'doi.label.button1text',
                    button2text: 'common.cancel',
                    autoClose: false
                  });
                } else {
                  scope.createDraft(intygType);
                }

              } else {
                scope.createDraft(intygType);
              }
            };

            scope.createDraft = function(intygType) {

              var createDraftRequestPayload = {
                intygType: intygType,
                patientPersonnummer: PatientModel.personnummer
              };
              createDraftRequestPayload.patientFornamn = PatientModel.fornamn;
              createDraftRequestPayload.patientMellannamn = PatientModel.mellannamn;
              createDraftRequestPayload.patientEfternamn = PatientModel.efternamn;
              createDraftRequestPayload.patientPostadress = PatientModel.postadress;
              createDraftRequestPayload.patientPostnummer = PatientModel.postnummer;
              createDraftRequestPayload.patientPostort = PatientModel.postort;
              ViewState.createErrorMessageKey = undefined;

              UtkastProxy.createUtkast(createDraftRequestPayload, function(data) {
                $location.url(
                    '/' + intygType + '/' + data.intygTypeVersion + '/edit/' + data.intygsId + '/', true);
              }, function(error) {
                $log.debug('Create draft failed: ' + error);
                if (error && error.errorCode === 'PU_PROBLEM') {
                  ViewState.createErrorMessageKey = 'error.pu_problem';
                } else {
                  ViewState.createErrorMessageKey = 'error.failedtocreateintyg';
                }
              });
            };

            scope.resolveIntygReplacedText = function(selectedIntygType) {
              var selectedIntyg = IntygTypeSelectorModel.intygTypes.filter(function(intygType) {
                return (intygType.id === selectedIntygType);
              })[0];
              var replacedIntygsType = scope.intygReplacement[selectedIntygType];
              var replacedIntyg = IntygTypeSelectorModel.intygTypes.filter(function(intygType) {
                return (intygType.id === replacedIntygsType);
              })[0];

              if (selectedIntyg && replacedIntyg) {
                return messageService.getProperty('info.intygstyp.replaced',
                    {oldIntygstyp: selectedIntyg.label, newIntygstyp: replacedIntyg.label});
              }

              return null;
            };
            scope.getDetailedDescription = function(intygsType) {
              var intygTypes = IntygTypeSelectorModel.intygTypes.filter(function(intygType) {
                return (intygType.id === intygsType);
              });
              if (intygTypes && intygTypes.length > 0) {
                return intygTypes[0].detailedDescription;
              }
            };

            function allowedToCreateUtkast(intygTypeId) {
              for (var i = 0; i < IntygTypeSelectorModel.userIntygTypes.length; i++) {
                if (IntygTypeSelectorModel.userIntygTypes[i].id === intygTypeId) {
                  return ResourceLinkService.isLinkTypeExists(
                      IntygTypeSelectorModel.userIntygTypes[i].links,
                      'SKAPA_UTKAST');
                }
              }
              return false;
            }

            scope.isCreateUtkastEnabled = function(intygType) {
              return !scope.intygReplacement[intygType] && allowedToCreateUtkast(intygType);
            };

            scope.getText = function(key) {
              return dynamicLabelService.getProperty(key);
            };
          }
        };
      }]
);
