$(function () {
    var users = [];
    var pendingDeleteId = null;
    var appConfig = Object.freeze(Object.assign({}, document.getElementById('app-config').dataset));
    var pageConfig = Object.freeze(Object.assign({}, document.getElementById('page-config').dataset));
    var api = createApiClient(appConfig.csrfUrl, {
        onUnauthorized: function () {
            window.location.replace(pageConfig.indexUrl);
        }
    });

    initializeSecurity();

    function initializeSecurity() {
        api.initialize().done(function () {
            bindHandlers();
            loadUsers();
        }).fail(function (request) {
            showRequestErrors(request, $('#panel-alert'));
        });
    }

    function bindHandlers() {

    $('#tbl-user').on('click', '.btn-update', function () {
        var id = Number($(this).data('index'));
        var user = users.find(function (item) { return item.idUser === id; });
        if (!user) return;
        $('#update-user [name="iduser"]').val(user.idUser);
        $('#update-user [name="firstname"]').val(user.firstName);
        $('#update-user [name="lastname"]').val(user.lastName);
        $('#update-user [name="email"]').val(user.email);
        $('#update-user [name="password"], #update-user [name="repeat-password"]').val('');
    });

    $('#tbl-user').on('click', '.btn-delete', function () {
        pendingDeleteId = Number($(this).data('index'));
    });

    $('#update-user').on('submit', function (event) {
        event.preventDefault();
        var form = $(this);
        var password = form.find('[name="password"]').val();
        if (password !== form.find('[name="repeat-password"]').val()) {
            showErrors({msg: 'The passwords do not match.'}, $('#modal-alert'));
            return;
        }
        api.put(appConfig.usersUrl + '/' + Number(form.find('[name="iduser"]').val()), {
            firstName: form.find('[name="firstname"]').val(),
            lastName: form.find('[name="lastname"]').val(),
            password: password
        }).done(function (result) {
            $('#updateModal').modal('hide');
            showSuccess(result.msg, $('#panel-alert'));
            loadUsers();
        }).fail(function (request) { showRequestErrors(request, $('#modal-alert')); });
    });

    $('#btn-delete-user').on('click', function () {
        if (!pendingDeleteId) return;
        api.delete(appConfig.usersUrl + '/' + pendingDeleteId)
          .done(function () { window.location.assign(pageConfig.indexUrl); })
          .fail(function (request) { showRequestErrors(request, $('#panel-alert')); });
    });

    $('#btn-logout').on('click', function () {
        api.delete(pageConfig.currentSessionUrl)
          .done(function () { window.location.assign(pageConfig.indexUrl); })
          .fail(function (request) { showRequestErrors(request, $('#panel-alert')); });
    });
    }

    function loadUsers() {
        api.get(appConfig.usersUrl).done(function (result) {
            users = result.data;
            var rows = users.map(function (user) {
                var actions = '<button class="btn btn-primary btn-update" data-index="' + user.idUser +
                    '" data-toggle="modal" data-target="#updateModal">Edit</button> ' +
                    '<button class="btn btn-danger btn-delete" data-index="' + user.idUser +
                    '" data-toggle="modal" data-target="#deleteModal">Delete</button>';
                return [user.idUser, user.firstName, user.lastName, user.email, actions];
            });
            $('#tbl-user').DataTable({
                data: rows,
                columns: [
                    {title: 'ID'},
                    {title: 'First Name', render: $.fn.dataTable.render.text()},
                    {title: 'Last Name', render: $.fn.dataTable.render.text()},
                    {title: 'Email', render: $.fn.dataTable.render.text()},
                    {title: 'Actions', orderable: false}
                ],
                destroy: true
            });
        }).fail(function (request) {
            showRequestErrors(request, $('#panel-alert'));
        });
    }

    function showRequestErrors(request, target) {
        var error = request.responseJSON || {msg: 'Request failed.'};
        if (request.status === 401 && error.code === 'AUTHENTICATION_REQUIRED') return;
        showErrors(error, target);
    }

    function showErrors(error, target) {
        target.removeClass('alert-success').addClass('alert-danger').empty()
            .append($('<strong>').text('Error! '))
            .append($('<span>').text(error.msg)).show();
    }

    function showSuccess(message, target) {
        target.removeClass('alert-danger').addClass('alert-success').text(message).show();
    }
});
