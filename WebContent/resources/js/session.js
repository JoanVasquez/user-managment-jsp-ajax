$(document).ready(function() {

	//DOM VARS
    var alert = $('#alert');
    var loginForm = $('#login-form');
    var registerForm = $('#register-form');

    //EVENT FUNCTIONS
    (function() {
        //CLOSE ALERT EVENT
        $('#panel-alert').on('click', '.close', function(event) {
            event.preventDefault();
            $(this).parent().hide();
        });
        //END CLOSE ALERT EVENT

        //CARD EVENT
        $('.fliper-btn').on('click', function() {
            $('.flip').find('.card').toggleClass('flipped');
        });
        //END CARD EVENT
        
      //CLOSE ALERTS EVENT
        alert.on('click', '.close', function(event) {
            event.preventDefault();
            $(this).parent().hide();
        });
        //END ALERT EVENT

        //LOGIN EVENT
        loginForm.on('submit', function(event) {
            event.preventDefault();
            var data = {
                "email": loginForm.find('input[name="email"]').val(),
                "password": loginForm.find('input[name="password"]').val(),
                "action": 'signin'
            }
            
            session(data);
        });
        //END LOGIN EVENT

        //FORGOTTEN PASSWORD EVENT
        $('#forgotten-password').on('click', function(event) {
            event.preventDefault();

            var data = {
                "email": loginForm.find('input[name="email"]').val(),
                "password": "none",
                "action": 'forgottenpassword'
            }
            forgottenPassword(data);
        });
        //END FORGOTTEN PASSWORD EVENT

        //SAVE USER EVENT
        registerForm.on('submit', function(event) {
            event.preventDefault();

            var password = registerForm.find('input[name="password"]').val();
            var passwordConfirmation = registerForm.find('input[name="repeatpassword"]').val();

            if (password === passwordConfirmation) {
                var data = {
                    "idUser": "0",
                    "firstName": registerForm.find('input[name="firstname"]').val(),
                    "lastName": registerForm.find('input[name="lastname"]').val(),
                    "email": registerForm.find('input[name="email"]').val(),
                    "password": registerForm.find('input[name="password"]').val(),
                    "action": "save"
                }
                saveUser(data);
            } else {
                var json = {
                    "notmatch": {
                        "message": "The password do not match each other!"
                    }
                }
                errorHandler(json)
            }
        });
        //END SAVE USER EVENT

    })();
    //END EVENT FUNCTIONS

    //SIGNIN FUNCTION
    function session(data) {
        console.log('***session***');

        $.ajax({
            url: 'session.co',
            dataType: 'json',
            method: 'POST',
            data: data,

            success: function(result) {
                var json = JSON.parse(result);
                if (json.success) window.location = "welcome.jsp";
            },

            error: function(request) {
                var json = JSON.parse(request.responseText);
                errorHandler(json);
            }
        });
    }

    //FORGOTTEN PASSWORD FUNCTION
    function forgottenPassword(data) {
        $.ajax({
            url: 'session.co',
            dataType: 'json',
            method: 'POST',
            data: data,

            success: function(result) {
                var json = JSON.parse(result);
                if (json.success) {
                    successFulHandler(json);
                }
            },

            error: function(request) {
                var json = JSON.parse(request.responseText);
                errorHandler(json);
            }
        });
    }

    //SAVE USER FUNCTION
    function saveUser(data) {
        console.log("***saveUser***");
        $.ajax({
            url: 'user.co',
            dataType: 'json',
            method: 'POST',
            data: data,

            success: function(result) {
                var json = JSON.parse(result);
                if (json.success) {
                    $('#register-form')[0].reset();
                    successFulHandler(json);
                }
            },

            error: function(request) {
                var json = JSON.parse(request.responseText);
                errorHandler(json);
            }
        });
    }

    //ALERT ON SUCCESSFUL
    function successFulHandler(result) {
        console.log('***successFulHandler***');

        alert.addClass('alert-success');
        alert.removeClass('alert-danger');
        alert.text('');

        alert.append('<strong>Message!</strong> Successful ');
        alert.append('<button type="button" class="close">x</button>');

        alert.show();
        
        setTimeout(function(){
        	alert.hide();
        }, 5000);
    }

    //ALERT ON ERROR
    function errorHandler(errors) {
        console.log('***errorHandler***');

        alert.removeClass('alert-success');
        alert.addClass('alert-danger');
        alert.text('');

        alert.append('<strong>Error!</strong> some errors have occurred ');
        alert.append('<button type="button" class="close">x</button>');
        alert.append('<ul>');
        $.each(errors, function(key, value) {
            alert.append('<li>' + value.message + '</li>');
        });
        alert.append('</ul>');

        alert.show();
        
        setTimeout(function(){
        	alert.hide();
        }, 5000);
    }
});