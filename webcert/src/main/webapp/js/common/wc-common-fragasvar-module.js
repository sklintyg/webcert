/**
 * Common Fragasvar Module - Common services and controllers related to FragaSvar
 * functionality to be used in webcert and modules handling Fraga/svar related to certificates.
 * (As of this time, only fk7263 module)
 */
angular.module('wc.common.fragasvarmodule', []);
angular.module('wc.common.fragasvarmodule').factory('fragaSvarCommonService', [ '$http', '$log', function($http, $log) {

    

    /*
     * Toggle vidarebefordrad state of a fragasvar entity with given id
     */
    function _setVidareBefordradState(id, isVidareBefordrad, callback) {
        $log.debug("_setVidareBefordradState");
        var restPath = '/moduleapi/fragasvar/' + id + "/setDispatchState";
        $http.put(restPath, isVidareBefordrad.toString()).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });
    }

   

    // Return public API for the service
    return {
        setVidareBefordradState : _setVidareBefordradState
    }
} ]);
