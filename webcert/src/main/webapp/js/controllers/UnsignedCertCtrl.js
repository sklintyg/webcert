define([
], function () {
    'use strict';

    /*
     * Controller for logic related to listing unsigned certs
     */
    return ['$scope', '$window', '$log', '$cookieStore', 'User', 'dashBoardService', function ($scope, $window, $log, $cookieStore, User, dashBoardService) {

        // Constant settings
        var PAGE_SIZE = 10;

        // Default view filter form widget states
        var defaultFilterFormData = {
            forwarded: "default",
            complete: "default",
            savedFromOpen: {
                open: false
            },
            savedToOpen: {
                open: false
            },
            savedByList: [ // doctor names select items
                {
                    label: 'Visa alla',
                    value: 'ALLA'
                }
            ],
            lastFilterQuery: {}
        };

        // Default API filter states
        var defaultFilter = {
            forwarded: undefined, // 3-state, undefined, true, false
            complete: undefined, // 3-state, undefined, true, false
            savedFrom: undefined,
            savedTo: undefined,
            savedBy: defaultFilterFormData.savedByList[0].value // selected doctor hasId
        };

        // Default query instance sent to search filter API
        var defaultFilterQuery = {
            enhetsId: User.getValdVardenhet().id,
            startFrom: 0,
            pageSize: PAGE_SIZE,
            filter: defaultFilter
        };
        defaultFilterFormData.lastFilterQuery = defaultFilterQuery;

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

            // Search states
            queryFormCollapsed: true,
            searchedYet: false,

            // List data
            totalCount: 0,
            currentList: undefined
        };

        /**
         *  Load initial data
         */
        resetFilterState();
        $scope.widgetState.doneLoading = false;

        dashBoardService.getUnsignedCertificates(function (data) {

                $scope.widgetState.doneLoading = true;
                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.currentList = data.results;
                $scope.widgetState.totalCount = data.totalCount;

            }, function () {

                $log.debug('Query Error');
                // TODO: real errorhandling
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';

            }
        );

        /**
         * Private functions
         */
        function resetFilterState() {
            $scope.filterForm = angular.copy(defaultFilterFormData);
        }

        /**
         * Exposed scope functions
         **/
        $scope.filterDrafts = function () {

            $log.debug('filterDrafts');
            $scope.widgetState.activeErrorMessageKey = null;
            var filterQuery = angular.copy(defaultFilterQuery);
            $scope.filterForm.lastFilterQuery = filterQuery;
            $cookieStore.put('enhetsId', filterQuery.enhetsId);
            $cookieStore.put('query_instance', filterQuery);

            dashBoardService.getUnsignedCertificatesByQueryFetchMore(filterQuery, function (successData) {

                $scope.widgetState.runningQuery = false;
                $scope.widgetState.currentList = successData.results;
                $scope.widgetState.totalCount = successData.totalCount;

            }, function () {

                $scope.widgetState.runningQuery = false;
                $log.debug('Query Error');
                // TODO: real errorhandling
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';

            });
        };

        $scope.resetFilter = function () {
            $cookieStore.remove('query_instance');
            resetFilterState();
        };

        $scope.fetchMore = function () {

            $log.debug('fetchMore');
            $scope.widgetState.activeErrorMessageKey = null;
            $scope.filterForm.lastFilterQuery.startFrom += PAGE_SIZE;
            $scope.widgetState.fetchingMoreInProgress = true;

            dashBoardService.getUnsignedCertificatesByQueryFetchMore($scope.filterForm.lastFilterQuery, function (successData) {
                $scope.widgetState.fetchingMoreInProgress = false;
                for (var i = 0; i < successData.results.length; i++) {
                    $scope.widgetState.currentList.push(successData.results[i]);
                }
            }, function () {
                $scope.widgetState.fetchingMoreInProgress = false;
                $log.debug('Query Error');
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';
            });
        };
    }];
});