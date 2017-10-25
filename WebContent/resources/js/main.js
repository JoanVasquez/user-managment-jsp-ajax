$(document).ready(function() {
    
	//PAGINATIONS AND FILTER VARS
	var page = 1;
    var totalPages = 0;
    var perPage = 8;
    var offSet = (page - 1) * perPage;
    var limit = page * perPage;
    var users = null;

    onInit();

    //EVENT FUNCTIONS
    (function() {
        //SET UPDATE VALUES - EVENT
        $("#tbl-user").on('click', '.btn-update', function(event) {
            event.preventDefault();
            var index = $(this).data('index');
            $.each(users, function(i, value) {
                if (index == value.idUser) {
                	console.log(value);
                    $('input[name="firstname"]').val(value.firstName);
                    $('input[name="lastname"]').val(value.lastName);
                    $('input[name="email"]').val(value.email);
                    $('input[name="password"]').val(value.tempPassword);
                    $('input[name="iduser"]').attr('value', value.idUser);
                }
            });
        });
        //EVEN SET VALUES - EVENT

        //UPDATE USER EVENT
        $('#update-user').on('submit', function(event) {
            event.preventDefault();

            var password = $('input[name="password"]').val();
            var repeatPassword = $('input[name="repeat-password"]').val();

            if (password === repeatPassword) {
                var data = {
                    "idUser": $('input[name="iduser"]').val(),
                    "firstName": $('input[name="firstname"]').val(),
                    "lastName": $('input[name="lastname"]').val(),
                    "email": $('input[name="email"]').val(),
                    "password": $('input[name="password"]').val(),
                    "action": "update"
                }
                updateUser(data);
                onInit();
            } else {
                var json = {
                    "notmatch": {
                        "message": "The password do not match each other!"
                    }
                }
                errorHandler(json)
            }
            //END UPDATE USER EVENT
        });

        //CLOSE ALERTS EVENT
        $('#modal-alert').on('click', '.close', function(event) {
            event.preventDefault();
            $(this).parent().hide();
        });

        $('#panel-alert').on('click', '.close', function(event) {
            event.preventDefault();
            $(this).parent().hide();
        });
        //END CLOSE ALERTS EVENT

        //DELETE USER EVENT
        $("#tbl-user").on('click', '.btn-delete', function(event) {
            event.preventDefault();
            var index = $(this).data('index');
            
            //MODAL DELETE
            $('#btn-delete-user').on('click', function(event) {
                event.preventDefault();
                deleteUser(index);
                isDeleted = true;
                $('#deleteModal').modal("hide");
                $("#tbl-user").DataTable().row($(this).parents("tr")).remove().draw();
            });
            
            

        });
        //END DELETE USER EVENT
        
        //LOG OUT EVENT
        $('#btn-logout').on('click', function(event) {
            event.preventDefault();
            logOut();
        });
        //END LOG OUT EVENT
    })();
    //END EVENT FUNCTIONS


    //GET USERS FROM THE SERVER
    function onInit() {
        console.log('***onInit***');
        $.ajax({
            url: 'user.co',
            dataType: 'json',
            method: 'GET',

            success: function(result) {
                users = result;
                generateTable();

            },

            error: function(request) {
                console.log(request.status);
            }
        });
    }

    //CREATE THE TABLE WITH THE USERS
    function generateTable() {
        console.log('***generateTable***');
        
        var userToArray = [];
        $.each(users, function(index, value){
        	if(users[index])
        		userToArray.push([value.idUser, value.firstName, value.lastName, value.email, '<button type="button" class="btn btn-primary btn-update" data-index="' + value.idUser + '" data-toggle="modal" data-target="#updateModal"><i class="glyphicon glyphicon-edit"></i></button> <button type="button" class="btn btn-danger btn-delete" data-index="' + value.idUser + '" data-toggle="modal" data-target="#deleteModal"><i class="glyphicon glyphicon-remove"></i></button>'])
        });
        
        
        $('#tbl-user').DataTable({
        	data: userToArray,
        	columns: [
        		{title: 'id User'},
        		{title: 'First Name'},
        		{title: 'Last Name'},
        		{title: 'email'},
        		{title: 'Actions'}
        	],
        	destroy: true
        });
    }

    //UPDATE AN USER
    function updateUser(user) {
        console.log('***updateUser***');
        $.ajax({
            url: 'user.co',
            dataType: 'json',
            method: 'POST',
            data: user,

            success: function(result) {
                var json = result;
                console.log(result);
                if (json.success) {
                    successFulHandler(json, 'modalmessage');
                    //onInit();
                    //console.log('called init...');
                }
            },

            error: function(request) {
                var json = request.responseText;
                errorHandler(json, 'modalerror');
            }
        });
    }

    //DELETE AN USER
    function deleteUser(index) {
        console.log('***deleteUser***');
        var data = {
            "idUser": index,
            "firstName": "none",
            "lastName": "none",
            "email": "none@none.com",
            "password": "none",
            "action": "delete"
        }
        $.ajax({
            url: 'user.co',
            dataType: 'json',
            method: 'POST',
            data: data,

            success: function(result) {
                var json = JSON.parse(result);
                if (json.success) {
                    successFulHandler(json, 'panelmessage');
                    onInit();
                } else if (json.redirect) window.location = "index.jsp";
            },

            error: function(request) {
                var json = request.responseText;
                errorHandler(json, 'panelerror');
            }
        });
    }

    //LOGOUT
    function logOut() {
        console.log('***logOut***');
        var data = {
            "email": "none@none.com",
            "password": "none",
            "action": "logout"
        }
        $.ajax({
            url: 'session.co',
            dataType: 'json',
            method: 'POST',
            data: data,

            success: function(result) {
                var json = JSON.parse(result);
                if (json.success) window.location = "index.jsp";
            },

            error: function(request) {
                var json = request.responseText;
                errorHandler(json, 'panelerror');
            }
        });
    }

    //ALERT ON SUCCESSFUL
    function successFulHandler(result, type) {
        console.log('***successFulHandler***');
        var alert;
        if (type === 'modalmessage') alert = $('#modal-alert');
        else alert = $('#panel-alert');

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
    function errorHandler(errors, type) {
        console.log('***errorHandler***');
        var alert;
        if (type === 'modalerror') alert = $('#modal-alert');
        else alert = $('#panel-alert');

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