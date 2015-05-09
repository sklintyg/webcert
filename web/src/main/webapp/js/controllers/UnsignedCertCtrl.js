/*
 * Controller for logic related to listing unsigned certs
 */
angular.module('webcert').controller('webcert.UnsignedCertCtrl',
    [ '$cookieStore', '$filter', '$location', '$log', '$scope', '$timeout', '$window', 'common.dialogService',
        'webcert.ManageCertificate', 'common.User', 'common.intygNotifyService', 'common.DateUtilsService',
        function($cookieStore, $filter, $location, $log, $scope, $timeout, $window, dialogService, ManageCertificate,
            User, intygNotifyService, dateUtilsService) {
            'use strict';

            // Constant settings
            var PAGE_SIZE = 10;

            // Exposed page state variables
            $scope.widgetState = {

                // User context
                valdVardenhet: User.getValdVardenhet(),

                // Loadíng states
                doneLoading: true,
                runningQuery: false,
                fetchingMoreInProgress: false,
                loadingSavedByList: false,

                // Error state
                activeErrorMessageKey: null,
                invalidFromDate : false,
                invalidToDate : false,

                // Search states
                queryFormCollapsed: true,
                filteredYet: false,
                savedByList: [],

                // List data
                totalCount: 0,
                currentList: undefined
            };

            // Default API filter states
            var defaultSavedByChoice = {
                name: 'Visa alla',
                hsaId: undefined
            };

            // Default query instance sent to search filter API
            var defaultFilterQuery = {
                enhetsId: User.getValdVardenhet().id,
                startFrom: 0,
                pageSize: PAGE_SIZE,
                filter: {
                    forwarded: undefined, // 3-state, undefined, true, false
                    complete: undefined, // 3-state, undefined, true, false
                    savedFrom: undefined,
                    savedTo: undefined,
                    savedBy: defaultSavedByChoice.hsaId // selected doctor hasId
                }
            };

            // Default view filter form widget states
            var defaultFilterFormData = {
                forwarded: 'default',
                complete: 'default',
                lastFilterQuery: defaultFilterQuery
            };

            function resetFilterState() {
                $scope.filterForm = angular.copy(defaultFilterFormData);
                $scope.widgetState.filteredYet = false;
            }

            function loadFilterForm() {

                resetFilterState();
                loadSavedByList($scope.widgetState.valdVardenhet);

                // Use saved choice if cookie has saved a filter
                var storedFilter = $cookieStore.get('unsignedCertFilter');
                if (storedFilter && storedFilter.filter.savedBy) {
                    $scope.filterForm.lastFilterQuery.filter.savedBy =
                        selectSavedByHsaId(storedFilter.filter.savedBy.hsaId);
                }
            }

            /**
             *  Load initial data
             */
            resetFilterState(); // Initializes $scope.filterForm from defaultFilterFormData
            loadFilterForm();
            $scope.widgetState.doneLoading = false;

            ManageCertificate.getUnsignedCertificates(function(data) {

                $scope.widgetState.doneLoading = true;
                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.currentList = data.results;
                $scope.widgetState.totalCount = data.totalCount;

                // These need to be after doneloading or they won't be available sometimes.
                dateUtilsService.addStrictDateParser($scope.filterFormElement['filter-changedate-from']);
                dateUtilsService.addStrictDateParser($scope.filterFormElement['filter-changedate-to']);

            }, function() {
                $log.debug('Query Error');
                $scope.widgetState.doneLoading = true;
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';
            });

            /**
             * Private functions
             */

            function selectSavedByHsaId(hsaId) {
                for (var count = 0; count < $scope.widgetState.savedByList.length; count++) {
                    if ($scope.widgetState.savedByList[count].hsaId === hsaId) {
                        return $scope.widgetState.savedByList[count];
                    }
                }
                return $scope.widgetState.savedByList[0];
            }

            function loadSavedByList() {

                $scope.widgetState.loadingSavedByList = true;

                ManageCertificate.getCertificateSavedByList(function(list) {
                    $scope.widgetState.loadingSavedByList = false;
                    $scope.widgetState.savedByList = list;
                    if (list && (list.length > 0)) {
                        $scope.widgetState.savedByList.unshift(defaultSavedByChoice);
                    }
                }, function() {
                    $scope.widgetState.loadingSavedByList = false;
                    $scope.widgetState.savedByList = [];
                    $scope.widgetState.savedByList.push({
                        hsaId: undefined,
                        name: '<Kunde inte hämta lista>'
                    });
                });
            }

            function convertFormFilterToPayload(query) {
                var filterQuery = angular.copy(query);
                var converted = filterQuery.filter;
                converted.enhetsId = filterQuery.enhetsId;
                converted.startFrom = filterQuery.startFrom;
                converted.pageSize = filterQuery.pageSize;
                converted.forwarded =
                        $scope.filterForm.forwarded !== 'default' ? $scope.filterForm.forwarded : undefined;
                converted.complete =
                        $scope.filterForm.complete !== 'default' ? $scope.filterForm.complete : undefined;
                converted.savedFrom = $filter('date')(converted.savedFrom, 'yyyy-MM-dd');
                if (converted.savedTo) {
                    // Date is used as datetime on backend
                    var to = moment(converted.savedTo);
                    to.add(1, 'd');
                    converted.savedTo = to.format('YYYY-MM-DD');
                }
                return converted;
            }

            function isToBeforeFrom() {
                var to = $scope.filterForm.lastFilterQuery.filter.savedTo;
                var from = $scope.filterForm.lastFilterQuery.filter.savedFrom;
                if (to === null || to === undefined || to === '') {
                    return false;
                }
                if (from === null || from === undefined || from === '') {
                    return false;
                }

                var mto = moment(to);
                var mfrom = moment(from);

                if(!mto.isValid() || !mfrom.isValid()) {
                    return false;
                }

                return moment(to).isBefore(from);
            }

            /**
             * Exposed scope functions
             **/
            $scope.filterDrafts = function() {
                $log.debug('filterDrafts');

                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.filteredYet = true;
                $scope.filterForm.lastFilterQuery.startFrom = 0;
                var filterQuery = $scope.filterForm.lastFilterQuery;
                $cookieStore.put('enhetsId', filterQuery.enhetsId);
                $cookieStore.put('unsignedCertFilter', $scope.filterForm.lastFilterQuery);
                filterQuery = convertFormFilterToPayload($scope.filterForm.lastFilterQuery);

                $scope.widgetState.invalidFromDate = $scope.filterFormElement['filter-changedate-from'].$error.date;
                $scope.widgetState.invalidToDate = $scope.filterFormElement['filter-changedate-to'].$error.date;
                $scope.widgetState.invalidToBeforeFromDate = isToBeforeFrom();


                $scope.widgetState.runningQuery = true;
                ManageCertificate.getUnsignedCertificatesByQueryFetchMore(filterQuery, function(successData) {
                    $scope.widgetState.runningQuery = false;
                    $scope.widgetState.currentList = successData.results;
                    $scope.widgetState.totalCount = successData.totalCount;
                }, function() {
                    $log.debug('Query Error');
                    $scope.widgetState.runningQuery = false;
                    $scope.widgetState.activeErrorMessageKey = 'info.query.error';
                });
            };

            $scope.resetFilter = function() {
                $cookieStore.remove('unsignedCertFilter');
                resetFilterState();
                $scope.filterDrafts();
            };

            $scope.fetchMore = function() {
                $log.debug('fetchMore');
                $scope.widgetState.activeErrorMessageKey = null;
                $scope.filterForm.lastFilterQuery.startFrom += PAGE_SIZE;
                var filterQuery = convertFormFilterToPayload($scope.filterForm.lastFilterQuery);
                $scope.widgetState.fetchingMoreInProgress = true;

                ManageCertificate.getUnsignedCertificatesByQueryFetchMore(filterQuery, function(successData) {
                    $scope.widgetState.fetchingMoreInProgress = false;
                    for (var i = 0; i < successData.results.length; i++) {
                        $scope.widgetState.currentList.push(successData.results[i]);
                    }
                }, function() {
                    $scope.widgetState.fetchingMoreInProgress = false;
                    $log.debug('Query Error');
                    $scope.widgetState.activeErrorMessageKey = 'info.query.error';
                });
            };

            $scope.openIntyg = function(cert) {
                $location.path('/' + cert.intygType + '/edit/' + cert.intygId);
            };

            // Handle forwarding
            $scope.openMailDialog = function(cert) {
                cert.updateState = {
                    vidarebefordradInProgress: true
                };
                intygNotifyService.forwardIntyg(cert, cert.updateState);
            };

            $scope.onForwardedChange = function(cert) {
                cert.updateState = {
                    vidarebefordradInProgress: true
                };
                intygNotifyService.onForwardedChange(cert, cert.updateState);
            };
        }]);
