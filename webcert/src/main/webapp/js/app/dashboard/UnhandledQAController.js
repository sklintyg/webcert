/*
 *  UnhandledQACtrl - Controller for logic related to listing questions and answers 
 * 
 */
angular
        .module('wcDashBoardApp')
        .controller(
                'UnhandledQACtrl',
                [
                        '$scope',
                        '$window',
                        '$log',
                        '$timeout',
                        '$filter',
                        'dashBoardService',
                        'fragaSvarCommonService',
                        'wcDialogService',
                        function UnhandledCertCtrl($scope, $window, $log, $timeout, $filter, dashBoardService, fragaSvarCommonService, wcDialogService) {
                            // init state
                            $scope.widgetState = {
                                doneLoading : false,
                                activeErrorMessageKey : null
                            }

                            $scope.isCollapsed = true;

                            $scope.qaList = {};
                            $scope.activeUnit = "";

                            // load all fragasvar for all units in usercontext
                            dashBoardService.getQA(function(data) {
                                $scope.widgetState.doneLoading = true;
                                if (data != null) {
                                    $scope.widgetState.activeErrorMessageKey = null;
                                    $scope.qaList = data;

                                    // set waiting messages
                                    angular.forEach($scope.qaList, function(qa, key) {

                                        if (qa.status == "ANSWERED" || qa.amne == "MAKULERING" || qa.amne == "PAMINNELSE") {
                                            qa.vantarpa = "markhandled";
                                        } else if (qa.status == "CLOSED") {
                                            qa.vantarpa = "handled";
                                        } else if (qa.amne == "KOMPLETTERING_AV_LAKARINTYG") {
                                            qa.vantarpa = "komplettering";
                                        } else {

                                            if (qa.status == "PENDING_INTERNAL_ACTION") {
                                                qa.vantarpa = "svarfranvarden";
                                            } else if (qa.status == "PENDING_EXTERNAL_ACTION") {
                                                qa.vantarpa = "svarfranfk";
                                            } else {
                                                qa.vantarpa = "";
                                                $log.debug("warning: undefined status");
                                            }
                                        }
                                    });

                                } else {
                                    $scope.widgetState.activeErrorMessageKey = "error.unansweredcerts.couldnotbeloaded";
                                }
                            });

                            $scope.onVidareBefordradChange = function(qa) {
                                qa.updateInProgress = true;
                                $timeout(
                                        function() { // wrap in timeout to
                                            // simulate
                                            // latency -
                                            fragaSvarCommonService
                                                    .setVidareBefordradState(
                                                            qa.internReferens,
                                                            qa.vidarebefordrad,
                                                            function(result) {
                                                                qa.updateInProgress = false;

                                                                if (result != null) {
                                                                    qa.vidarebefordrad = result.vidarebefordrad;
                                                                } else {
                                                                    qa.vidarebefordrad = !qa.vidarebefordrad;
                                                                    wcDialogService
                                                                            .showErrorMessageDialog("Kunde inte markera/avmarkera frågan som vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. Annars kan du kontakta supporten");
                                                                }
                                                            });
                                        }, 1000);
                            }

                            $scope.setActiveUnit = function(unit) {
                                $log.debug("ActiveUnit is now:" + unit);
                                $scope.activeUnit = unit;
                            }

                            // Calculate how many entities we have for a
                            // specific
                            // enhetsId
                            $scope.getItemCountForUnitId = function(unit) {
                                if (!$scope.widgetState.doneLoading) {
                                    return "?";
                                }
                                var count = $filter('QAEnhetsIdFilter')($scope.qaList, unit.id).length;

                                return count;
                            }

                            $scope.openIntyg = function(intygsReferens) {
                                $log.debug("open intyg " + intygsReferens.intygsId);
                                $window.location.href = "/m/" + intygsReferens.intygsTyp.toLowerCase() + "/webcert/intyg/" + intygsReferens.intygsId;
                            }

                            // Handle vidarebefordra dialog
                            $scope.openMailDialog = function(qa) {
                                $timeout(function() {
                                    fragaSvarCommonService.handleVidareBefodradToggle(qa, $scope.onVidareBefordradChange);
                                }, 1000);
                                //Launch mail client
                                $window.location = fragaSvarCommonService.buildMailToLink(qa);
                            }

                            // Search filter date controls
                            $scope.today = function() {
                                $scope.dtF = new Date();
                                $scope.dtT = new Date();
                                $scope.dtA = new Date();
                            };
                            $scope.today();

                            $scope.showWeeks = false;
                            $scope.minDate = null;

                            $scope.dpFromOpen = false;
                            $scope.openDPFrom = function() {
                                $timeout(function() {
                                    $scope.dpFromOpen = !$scope.dpFromOpen;
                                });
                            };

                            $scope.dpToOpen = false;
                            $scope.openDPTo = function() {
                                $timeout(function() {
                                    $scope.dpToOpen = !$scope.dpToOpen;
                                });
                            };

                            $scope.dpAnswerOpen = false;
                            $scope.openDPAnswer = function() {
                                $timeout(function() {
                                    $scope.dpAnswerOpen = !$scope.dpAnswerOpen;
                                });
                            };

                            $scope.dateOptions = {
                                'year-format' : "'yy'",
                                'starting-day' : 1
                            };

                        } ]);