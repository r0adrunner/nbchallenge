// define angular module/app
var formApp = angular.module('formApp', []);

// create angular controller and pass in $scope and $http
function formController($scope, $http) {
    $scope.formData = {};

    // process the form
    $scope.processForm = function() {

	$scope.formData.isfraudulent = "true";

	$http({
	    method  : 'PUT',
	    url     : '/node',
	    data    : $.param($scope.formData),  // pass in data as strings
	    headers : { 'Content-Type': 'application/x-www-form-urlencoded' }  // set the headers so angular passing info as form data (not request payload)
	})
	// $http.post('process.php', $scope.formData)
	    .success(function(data) {
		console.log(data);
		// if successful, bind success message to message
		$scope.message = "";
		$scope.message = data.message;
		$scope.errorNode = "";
	    })
	    .error(
		function(data) {
		    console.log(data);
		    $scope.message = data.message;
		    // if not successful, bind errors to error variables
		    $scope.errorNode = "";
		    $scope.errorNode = data.errors.errorNode;
		});

    };
}
