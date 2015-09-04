/**
 * Created by stephenwhite on 31/08/15.
 */
angular.module('webcert.pub.login', ['ui.bootstrap'])
    .controller('LoginController', ['$scope', '$sce','$modal', function($scope, $sce, $modal) {
        var expand = $sce.trustAsHtml('Visa mer om inloggning <span class="glyphicon glyphicon-chevron-down"></span>');
        var collapse = $sce.trustAsHtml('Visa mindre om inloggning <span class="glyphicon glyphicon-chevron-up"></span>');
        $scope.collapseLoginDesc = true;
        $scope.loginDescText = expand;
        $scope.toggleLoginDesc = function(){
            $scope.collapseLoginDesc = !$scope.collapseLoginDesc;
            if($scope.collapseLoginDesc){
                $scope.loginDescText = expand;
            } else {
                $scope.loginDescText = collapse;
            }
        };

        $scope.open = function (which) {

            $scope.modalInstance = $modal.open({
                templateUrl: which,
                scope: $scope,
                size: 'lg'
            });

            $scope.modalInstance.result.then(function (selectedItem) {
                $scope.selected = selectedItem;
            }, function () {
                // closed the modal
            });
        };

        $scope.ok = function () {
            $scope.modalInstance.close();
        };

    }]);