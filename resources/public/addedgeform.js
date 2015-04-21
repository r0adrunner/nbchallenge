// define angular module/app
var formApp = angular.module('formApp', []);

// create angular controller and pass in $scope and $http
function formController($scope, $http) {
    $scope.formData = {};

    // process the form
    $scope.processForm = function() {

	$http({
	    method  : 'POST',
	    url     : '/edge',
	    data    : $.param($scope.formData),  // pass in data as strings
	    headers : { 'Content-Type': 'application/x-www-form-urlencoded' }  // set the headers so angular passing info as form data (not request payload)
	})
	// $http.post('process.php', $scope.formData)
	    .success(function(data) {
		console.log(data);
		// if successful, bind success message to message
		$scope.message = data.message;
		$scope.errorNode1 = "";
		$scope.errorNode2 = "";
	    })
	    .error(
		function(data) {
		    console.log(data);
		    $scope.message = data.message;
		    // if not successful, bind errors to error variables
		    $scope.errorNode1 = "";
		    $scope.errorNode2 = "";
		    $scope.errorNode1 = data.errors.errorNode1;
		    $scope.errorNode2 = data.errors.errorNode2;
		});

    };
}
