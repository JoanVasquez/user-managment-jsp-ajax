$(function () {
    var alertBox = $('#alert');
    var loginForm = $('#login-form');
    var registerForm = $('#register-form');
    var appConfig = Object.freeze(Object.assign({}, document.getElementById('app-config').dataset));
    var pageConfig = Object.freeze(Object.assign({}, document.getElementById('page-config').dataset));
    var api = createApiClient(appConfig.csrfUrl);

    initializeSecurity();

    function initializeSecurity() {
        api.initialize().done(function () {
            bindHandlers();
        }).fail(function () {
            showErrors({msg: 'Unable to initialize request security.'});
        });
    }

    function bindHandlers() {

    $('.fliper-btn').on('click', function (event) {
        event.preventDefault();
        $('.flip').find('.card').toggleClass('flipped');
    });

    alertBox.on('click', '.close', function () { alertBox.hide(); });

    loginForm.on('submit', function (event) {
        event.preventDefault();
        api.post(pageConfig.sessionsUrl, {
            email: loginForm.find('[name="email"]').val(),
            password: loginForm.find('[name="password"]').val()
        }).done(function () { window.location.assign(pageConfig.welcomeUrl); })
          .fail(showRequestErrors);
    });

    $('#forgotten-password').on('click', function (event) {
        event.preventDefault();
        api.post(pageConfig.passwordResetRequestsUrl, {
            email: loginForm.find('[name="email"]').val() || 'unknown@example.com'
        }).done(function (result) {
            showSuccess(result.msg);
        }).fail(showRequestErrors);
    });

    registerForm.on('submit', function (event) {
        event.preventDefault();
        var password = registerForm.find('[name="password"]').val();
        if (password !== registerForm.find('[name="repeatpassword"]').val()) {
            showErrors({msg: 'The passwords do not match.'});
            return;
        }
        api.post(appConfig.usersUrl, {
            firstName: registerForm.find('[name="firstname"]').val(),
            lastName: registerForm.find('[name="lastname"]').val(),
            email: registerForm.find('[name="email"]').val(),
            password: password
        }).done(function (result) {
            registerForm[0].reset();
            showSuccess(result.msg);
        }).fail(showRequestErrors);
    });
    }

    function showRequestErrors(request) {
        showErrors(request.responseJSON || {msg: 'Request failed.'});
    }

    function showErrors(error) {
        alertBox.removeClass('alert-success').addClass('alert-danger').empty()
            .append($('<strong>').text('Error! ')).append($('<span>').text(error.msg))
            .append($('<button type="button" class="close">').text('×')).show();
    }

    function showSuccess(message) {
        alertBox.removeClass('alert-danger').addClass('alert-success').empty()
            .append($('<strong>').text(message))
            .append($('<button type="button" class="close">').text('×')).show();
    }
});
