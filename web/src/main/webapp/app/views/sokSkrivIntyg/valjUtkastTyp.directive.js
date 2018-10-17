angular.module('webcert').directive('wcValjUtkastTyp',
    [ '$log', '$location', 'webcert.SokSkrivIntygViewstate', 'common.messageService', 'common.featureService',
        'common.PatientModel', 'webcert.UtkastProxy', 'common.dialogService',
    function($log, $location, ViewState, messageService, featureService, PatientModel, UtkastProxy, DialogService) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                viewState: '=',
                domId: '@'
            },
            templateUrl: '/app/views/sokSkrivIntyg/valjUtkastTyp.directive.html',
            link: function(scope, element, attrs) {

                var IntygTypeSelectorModel = scope.viewState.IntygTypeSelectorModel;
                scope.intygTypeModel = IntygTypeSelectorModel;
                scope.intygReplacement = {
                    'fk7263':'lisjp'
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
                    if(intygType === 'doi') {
                        var intygList = ViewState.intygListUnhandled;
                        var dbExists = false;

                        for(var i = 0; i < intygList.length; i++) {
                           if(intygList[i].intygType === 'db' && intygList[i].status === 'SENT') {
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
                                    button2click: function(modalInstance){
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
                        $location.url('/' + intygType + '/' + data.intygTypeVersion + '/edit/' + data.intygsId + '/', true);
                    }, function(error) {
                        $log.debug('Create draft failed: ' + error);
                        if (error && error.errorCode === 'PU_PROBLEM') {
                            ViewState.createErrorMessageKey = 'error.pu_problem';
                        } else {
                            ViewState.createErrorMessageKey = 'error.failedtocreateintyg';
                        }
                    });
                };

                scope.resolveIntygReplacedText = function (selectedIntygType) {
                    var selectedIntyg = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === selectedIntygType);
                    })[0];
                    var replacedIntygsType = scope.intygReplacement[selectedIntygType];
                    var replacedIntyg = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === replacedIntygsType);
                    })[0];

                    if(selectedIntyg && replacedIntyg){
                        return messageService.getProperty('info.intygstyp.replaced', {oldIntygstyp: selectedIntyg.label, newIntygstyp: replacedIntyg.label});
                    }

                    return null;
                };
                scope.getDetailedDescription = function (intygsType) {
                    var intygTypes = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === intygsType);
                    });
                    if (intygTypes && intygTypes.length > 0) {
                        return intygTypes[0].detailedDescription;
                    }
                };

                function existsInUserTypes(intygTypeId) {
                    for (var i = 0; i < IntygTypeSelectorModel.userIntygTypes.length; i++) {
                        if(IntygTypeSelectorModel.userIntygTypes[i].id === intygTypeId) {
                            return true;
                        }
                    }
                    return false;
                }

                scope.isCreateUtkastEnabled = function (intygType) {
                    // Måste ha valt en intygstyp
                    // Intygstypen får inte vara deprecated
                    // Måste uppfylla ev unikhetskrav för intyg/utkast inom/utom vg
                    // Måste finnas med i listan över användarutkast
                    return !scope.intygReplacement[intygType] &&
                        existsInUserTypes(intygType) &&
                        scope.passesUniqueIntygWithinCareGiverCheck(intygType) &&
                        scope.passesUniqueUtkastWithinCareGiverCheck(intygType) &&
                        scope.passedUniqueGlobalCheck(intygType);
                };

                // Har intygstypen begränsning som säger att inga andra intyg av typen får finnas på samma vg för patienten?
                scope.passesUniqueIntygWithinCareGiverCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG_INOM_VG, intygType);
                    return !featureActive || !IntygTypeSelectorModel.previousIntygWarnings[intygType] || !IntygTypeSelectorModel.previousIntygWarnings[intygType].sameVardgivare;
                };
                // Har intygstypen begränsning som säger att inga andra utkast av typen får finnas på samma vg för patienten?
                scope.passesUniqueUtkastWithinCareGiverCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_UTKAST_INOM_VG, intygType);
                    return !featureActive || !IntygTypeSelectorModel.previousUtkastWarnings[intygType] || !IntygTypeSelectorModel.previousUtkastWarnings[intygType].sameVardgivare;
                };

                // Har intygstypen begränsning som säger att inga andra intyg av typen får finnas utfärdade på någon vg för patienten?
                scope.passedUniqueGlobalCheck = function (intygType) {
                    var featureActive = featureService.isFeatureActive(featureService.features.UNIKT_INTYG, intygType);
                    return !featureActive || !IntygTypeSelectorModel.previousIntygWarnings[intygType];
                };
            }
        };
    }]
);
