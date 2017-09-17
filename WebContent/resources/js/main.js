$(document).ready(function() {
    
	//PAGINATIONS AND FILTER VARS
	var page = 1;
    var totalPages = 0;
    var perPage = 8;
    var offSet = (page - 1) * perPage;
    var limit = page * perPage;
    var users = null;
    var searchString = '';

    onInit();

    //DOM VARS
    var inputSearch = $('#search');
    var ulPaging = $('#pagination');
    var liPaging;
    var liFirst;
    var liLast;
    var lastPage;
    var tblUser = $('#tbl-users');
    var btnDeleteUser = $('#btn-delete-user');

    //EVENT FUNCTIONS
    (function() {
        //FILTER EVENT
        inputSearch.keyup(function() {
            searchString = inputSearch.val();
            if (searchString) {
                ulPaging.hide();
                generateTable('filter');
            } else {
                ulPaging.show();
                generateTable('init');
            }
        });
        //END FILTER EVENT

        //SET UPDATE VALUES - EVENT
        tblUser.on('click', '.btn-update', function(event) {
            event.preventDefault();
            var index = $(this).data('index');
            $.each(users, function(i, value) {
                if (index == value.idUser) {
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
        tblUser.on('click', '.btn-delete', function(event) {
            event.preventDefault();
            var index = $(this).data('index');

            btnDeleteUser.on('click', function(event) {
                event.preventDefault();
                deleteUser(index);
                $('#deleteModal').modal('hide');
            });

        });
        //END DELETE USER EVENT

        //GO TO FIRST PAGE EVENT
        ulPaging.on('click', '#start-page', function(event) {
            event.preventDefault();
            page = 1;
            offSet = (page - 1) * perPage;
            limit = page * perPage;

            if (!liFirst.parent().hasClass('disabled')) generateTable('init');

            liFirst.parent().removeClass('disabled').addClass('disabled');

            liLast.parent().removeClass('disabled');

            liPaging.removeClass('active');

            liPaging.first().addClass('active');

        });
        //END GO TO FIRST PAGE EVENT

        //PAGE NAVS EVENT
        ulPaging.on('click', '#page-links', function(event) {
            event.preventDefault();
            page = parseInt($(this).text());

            page > 1 ? liFirst.parent().removeClass('disabled') : liFirst.parent().addClass('disabled');

            offSet = (page - 1) * perPage;
            limit = page * perPage;

            page >= lastPage ? liLast.parent().addClass('disabled') : liLast.parent().removeClass('disabled');

            if (!$(this).parent().hasClass('active')) generateTable('init');

            liPaging.removeClass('active');

            $(this).parent().addClass('active');

        });

        //END PAGE NAVS EVENT
        ulPaging.on('click', '#last-page', function(event) {
            event.preventDefault();
            page = lastPage;

            offSet = (page - 1) * perPage;
            limit = page * perPage;

            if (!liLast.parent().hasClass('disabled')) generateTable('init');

            liPaging.removeClass('active');

            liPaging.last().addClass('active');

            liFirst.parent().removeClass('disabled');
            liLast.parent().addClass('disabled');

        });
        //GO TO LAST PAGE EVENT

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
                totalPages = users.length;

                generatePagingButtons();
                generateTable('init');

            },

            error: function(request) {
                console.log(request.status);
            }
        });
    }

    //CREATE PAGING BUTTONS
    function generatePagingButtons() {
        console.log('***generatePagingButtons***');
        
        var numPages = Math.ceil(totalPages / perPage); //GET THE AMOUNT OF PAGES
        
        if (numPages > 0) {
            ulPaging.children('li').remove();
            ulPaging.append('<li class="disabled"><a href="#" id="start-page"> << </a> </li>');

            for (var i = 0; i < numPages; i++) ulPaging.append('<li class="li-page"><a href="#" id="page-links">' + (i + 1) + '</a></li>');

            ulPaging.append('<li><a href="#" id="last-page"> >> </a> </li>');

            liFirst = $('#start-page');
            lastPage = Math.ceil(totalPages / perPage);
            liLast = $('#last-page');

            liPaging = $('.li-page');

            liPaging.first().addClass('active');
        }
    }

    //CREATE THE TABLE WITH THE USERS
    function generateTable(isInitOrFilter) {
        console.log('***generateTable***');

        tblUser.children('tr').remove();

        if (isInitOrFilter === 'init') {
            for (var i = offSet; i < limit; i++)
                if (users[i]) tblUser.append('<tr><td><button type="button" class="btn btn-primary btn-update" data-index="' + users[i].idUser + '" data-toggle="modal" data-target="#updateModal"><i class="glyphicon glyphicon-edit"></i></button> <button type="button" class="btn btn-danger btn-delete" data-index="' + users[i].idUser + '" data-toggle="modal" data-target="#deleteModal"><i class="glyphicon glyphicon-remove"></i></button></td><td>' + users[i].idUser + '</td><td>' + users[i].firstName + '</td><td>' + users[i].lastName + '</td><td>' + users[i].email + '</td></tr>');
                else break;
        } else {
            $.each(users, function(index, value) {
                if ((users[index].firstName.toLowerCase().includes(searchString.toLowerCase())) || (users[index].lastName.toLowerCase().includes(searchString.toLowerCase())) ||
                    (users[index].email.toLowerCase().includes(searchString.toLowerCase())))
                    tblUser.append('<tr><td><button type="button" class="btn btn-primary btn-update" data-index="' + value.idUser + '" data-toggle="modal" data-target="#updateModal"><i class="glyphicon glyphicon-edit"></i></button> <button type="button" class="btn btn-danger btn-delete" data-index="' + value.idUser + '" data-toggle="modal" data-target="#deleteModal"><i class="glyphicon glyphicon-remove"></i></button></td><td>' + value.idUser + '</td><td>' + value.firstName + '</td><td>' + value.lastName + '</td><td>' + value.email + '</td></tr>');
            });
        }
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
                var json = JSON.parse(result);
                if (json.success) {
                    successFulHandler(json, 'modalmessage');
                    onInit();
                    console.log('called init...');
                }
            },

            error: function(request) {
                var json = JSON.parse(request.responseText);
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
                var json = JSON.parse(request.responseText);
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
                var json = JSON.parse(request.responseText);
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