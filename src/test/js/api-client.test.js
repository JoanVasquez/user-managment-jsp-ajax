'use strict';

var assert = require('node:assert/strict');
var test = require('node:test');
var createApiClient = require('../../main/resources/static/resources/js/api-client.js');
var CURRENT_ORIGIN = 'https://app.example.com';

function fakeJQuery(csrfResponses, ajaxResponses) {
    var calls = {csrf: [], ajax: []};

    return {
        calls: calls,
        getJSON: function (url) {
            calls.csrf.push(url);
            return responsePromise(csrfResponses.shift());
        },
        ajax: function (options) {
            calls.ajax.push(options);
            return responsePromise(ajaxResponses.shift());
        },
        extend: function () {
            return Object.assign.apply(Object, arguments);
        }
    };
}

function responsePromise(response) {
    var promise = response && response.rejection
            ? Promise.reject(response.rejection)
            : Promise.resolve(response);
    return decorate(promise);
}

function decorate(promise) {
    var nativeThen = promise.then.bind(promise);
    promise.then = function (onFulfilled, onRejected) {
        return decorate(nativeThen(onFulfilled, onRejected));
    };
    promise.done = function (callback) {
        nativeThen(callback);
        return promise;
    };
    promise.fail = function (callback) {
        nativeThen(null, callback);
        return promise;
    };
    return promise;
}

function csrf(token) {
    return {headerName: 'X-CSRF-TOKEN', token: token};
}

function success(data) {
    return {code: 'SUCCESS', msg: 'Success.', data: data};
}

function csrfFailure(message) {
    return {
        status: 403,
        responseJSON: {code: 'INVALID_CSRF_TOKEN', msg: message, data: null}
    };
}

test('unsafe JSON requests include the current CSRF header', async function () {
    var jquery = fakeJQuery([success(csrf('token-1'))], [success({idUser: 1})]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await api.post('/api/users', {firstName: 'Ada'});

    assert.deepEqual(jquery.calls.ajax[0], {
        url: '/api/users',
        method: 'POST',
        headers: {'X-CSRF-TOKEN': 'token-1'},
        contentType: 'application/json',
        dataType: 'json',
        data: '{"firstName":"Ada"}'
    });
});

test('safe GET requests do not include a CSRF header', async function () {
    var jquery = fakeJQuery([success(csrf('token-1'))], [success([{idUser: 1}])]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await api.get('/api/users');

    assert.deepEqual(jquery.calls.ajax[0].headers, {});
});

test('same-origin absolute and protocol-relative URLs are allowed', async function () {
    var jquery = fakeJQuery(
            [success(csrf('token-1'))],
            [success([{idUser: 1}]), success([{idUser: 2}])]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await api.get(CURRENT_ORIGIN + '/api/users');
    await api.get('//app.example.com/api/users/2');

    assert.deepEqual(jquery.calls.ajax.map(function (request) {
        return request.url;
    }), [
        CURRENT_ORIGIN + '/api/users',
        '//app.example.com/api/users/2'
    ]);
});

test('custom request options and headers are copied instead of mutated', async function () {
    var jquery = fakeJQuery([success(csrf('token-1'))], [success({ok: true})]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);
    var options = {
        url: '/api/users',
        method: 'POST',
        headers: {'X-Request-ID': 'request-1'}
    };

    await api.request(options);

    assert.deepEqual({
        original: options,
        sentHeaders: jquery.calls.ajax[0].headers
    }, {
        original: {
            url: '/api/users',
            method: 'POST',
            headers: {'X-Request-ID': 'request-1'}
        },
        sentHeaders: {
            'X-Request-ID': 'request-1',
            'X-CSRF-TOKEN': 'token-1'
        }
    });
});

test('client requests retain jQuery done and fail callbacks', function () {
    var jquery = fakeJQuery([success(csrf('token-1'))], [success([{idUser: 1}])]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    var request = api.get('/api/users');

    assert.deepEqual(
            {done: typeof request.done, fail: typeof request.fail},
            {done: 'function', fail: 'function'});
});

test('cross-origin requests and whitespace-prefixed URLs are rejected before loading a CSRF token', function () {
    var jquery = fakeJQuery([], []);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);
    var invalidUrls = [
        'https://example.com/api/users',
        '//example.com/api/users',
        'http://app.example.com/api/users',
        'https://app.example.com:8443/api/users',
        'javascript:alert(1)',
        ' https://example.com/api/users',
        '\thttps://example.com/api/users',
        '\n//example.com/api/users'
    ];

    invalidUrls.forEach(function (url) {
        assert.throws(
                function () {
                    api.request({url: url, method: 'POST'});
                },
                /same-origin/);
    });
    assert.equal(jquery.calls.csrf.length, 0);
});

test('a CSRF rejection refreshes the token and retries once', async function () {
    var rejection = csrfFailure('Expired');
    var jquery = fakeJQuery(
            [success(csrf('token-1')), success(csrf('token-2'))],
            [{rejection: rejection}, success({idUser: 1})]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    var result = await api.put('/api/users/1', {firstName: 'Ada'});

    assert.deepEqual({
        result: result,
        csrfCalls: jquery.calls.csrf.length,
        ajaxCalls: jquery.calls.ajax.length,
        retryToken: jquery.calls.ajax[1].headers['X-CSRF-TOKEN']
    }, {
        result: success({idUser: 1}),
        csrfCalls: 2,
        ajaxCalls: 2,
        retryToken: 'token-2'
    });
});

test('concurrent CSRF rejections share one token refresh', async function () {
    var rejection = csrfFailure('Expired');
    var jquery = fakeJQuery(
            [success(csrf('token-1')), success(csrf('token-2'))],
            [{rejection: rejection}, {rejection: rejection}, success({ok: 1}), success({ok: 2})]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await api.initialize();
    var results = await Promise.all([
        api.delete('/api/users/1'),
        api.delete('/api/users/2')
    ]);

    assert.deepEqual({
        results: results,
        csrfCalls: jquery.calls.csrf.length,
        ajaxCalls: jquery.calls.ajax.length
    }, {
        results: [success({ok: 1}), success({ok: 2})],
        csrfCalls: 2,
        ajaxCalls: 4
    });
});

test('a second CSRF rejection is returned without another retry', async function () {
    var first = csrfFailure('Expired');
    var second = csrfFailure('Still invalid');
    var jquery = fakeJQuery(
            [success(csrf('token-1')), success(csrf('token-2'))],
            [{rejection: first}, {rejection: second}]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.delete('/api/users/1'), function (error) {
        return error === second;
    });
    assert.deepEqual(
            {csrfCalls: jquery.calls.csrf.length, ajaxCalls: jquery.calls.ajax.length},
            {csrfCalls: 2, ajaxCalls: 2});
});

test('a generic 403 with a csrf-shaped body does not refresh the token', async function () {
    var rejection = {status: 403, responseJSON: {code: 'ACCESS_DENIED', msg: 'Denied', data: null}};
    var jquery = fakeJQuery([success(csrf('token-1'))], [{rejection: rejection}]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.delete('/api/users/1'), function (error) {
        return error === rejection;
    });
    assert.deepEqual(
            {csrfCalls: jquery.calls.csrf.length, ajaxCalls: jquery.calls.ajax.length},
            {csrfCalls: 1, ajaxCalls: 1});
});

test('invalid credentials do not invoke the unauthorized handler', async function () {
    var rejection = {
        status: 401,
        responseJSON: {code: 'INVALID_CREDENTIALS', msg: 'Invalid', data: null}
    };
    var jquery = fakeJQuery([success(csrf('token-1'))], [{rejection: rejection}]);
    var handled = 0;
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN, {
        onUnauthorized: function () { handled += 1; }
    });

    await assert.rejects(api.get('/api/users'), function (error) {
        return error === rejection;
    });
    assert.deepEqual({csrfCalls: jquery.calls.csrf.length, handled: handled}, {
        csrfCalls: 1,
        handled: 0
    });
});

test('authentication failures invoke the optional unauthorized handler once', async function () {
    var rejection = {
        status: 401,
        responseJSON: {code: 'AUTHENTICATION_REQUIRED', msg: 'Required', data: null}
    };
    var jquery = fakeJQuery(
            [success(csrf('token-1'))],
            [{rejection: rejection}, {rejection: rejection}]);
    var handledRequests = [];
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN, {
        onUnauthorized: function (request) {
            handledRequests.push(request);
        }
    });

    await Promise.allSettled([
        api.get('/api/users'),
        api.get('/api/users')
    ]);

    assert.deepEqual(handledRequests, [rejection]);
    assert.equal(jquery.calls.csrf.length, 1);
});

test('authorization failures do not refresh the CSRF token', async function () {
    var rejection = {status: 403, responseJSON: {code: 'ACCESS_DENIED', msg: 'Denied', data: null}};
    var jquery = fakeJQuery([success(csrf('token-1'))], [{rejection: rejection}]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.delete('/api/users/1'), function (error) {
        return error === rejection;
    });
    assert.equal(jquery.calls.csrf.length, 1);
});

test('a failed CSRF refresh does not resend the original mutation', async function () {
    var csrfRejection = csrfFailure('Expired');
    var refreshRejection = {status: 500};
    var jquery = fakeJQuery(
            [success(csrf('token-1')), {rejection: refreshRejection}],
            [{rejection: csrfRejection}]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.delete('/api/users/1'), function (error) {
        return error === refreshRejection;
    });
    assert.equal(jquery.calls.ajax.length, 1);
});

test('CSRF initialization can be attempted again after a failure', async function () {
    var firstFailure = {status: 500};
    var jquery = fakeJQuery([{rejection: firstFailure}, success(csrf('token-2'))], []);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.initialize());
    var result = await api.initialize();

    assert.deepEqual(
            {result: result, csrfCalls: jquery.calls.csrf.length},
            {result: csrf('token-2'), csrfCalls: 2});
});

test('CSRF initialization rejects malformed success envelopes', async function () {
    var responses = [
        {code: 'INVALID_CSRF_TOKEN', msg: 'Wrong code.', data: csrf('token-1')},
        success({headerName: '', token: 'token-2'}),
        success({headerName: 'X-CSRF-TOKEN', token: ''})
    ];

    for (var index = 0; index < responses.length; index += 1) {
        var jquery = fakeJQuery([responses[index]], []);
        var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

        await assert.rejects(api.initialize(), /CSRF response is invalid/);
    }
});

test('CSRF initialization can recover after a malformed success envelope', async function () {
    var jquery = fakeJQuery([
        success({headerName: '', token: 'invalid'}),
        success(csrf('token-2'))
    ], []);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    await assert.rejects(api.initialize(), /CSRF response is invalid/);
    var result = await api.initialize();

    assert.deepEqual(result, csrf('token-2'));
    assert.equal(jquery.calls.csrf.length, 2);
});

test('successful responses preserve the response wrapper', async function () {
    var response = success(null);
    var jquery = fakeJQuery([success(csrf('token-1'))], [response]);
    var api = createApiClient(jquery, '/api/csrf', CURRENT_ORIGIN);

    var result = await api.delete('/api/sessions/current');

    assert.deepEqual(result, response);
});
