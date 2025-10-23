$(document).click(function() {
    $('#loginForm').submit(function(e) {
        e.preventDefault(); // Prevent form submission
        const submitButton = $('#loginBtn');

        setTimeout(function() {
            submitButton.html(
                '<i class="fa fa-circle-o-notch fa-spin"></i> Logging in...'
            );

            // Submit the form
            $('#loginForm')[0].submit();
        }, 1000);
    });

    $('#signUpForm').submit(function(e) {
        e.preventDefault(); // Prevent form submission
        const submitButton = $('#signUpBtn');

        setTimeout(function() {
            submitButton.html(
                '<i class="fa fa-circle-o-notch fa-spin"></i> Creating account...'
            );

            // Submit the form
            $('#signUpForm')[0].submit();
        }, 1000);
    });
});