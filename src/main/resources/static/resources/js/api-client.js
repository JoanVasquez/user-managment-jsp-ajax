(function (root, factory) {
    'use strict';

    if (typeof module === 'object' && module.exports) {
        module.exports = factory;
        return;
    }

    root.createApiClient = function (csrfUrl, options) {
        return factory(root.jQuery, csrfUrl, root.location.origin, options);
    };
}(typeof self !== 'undefined' ? self : this, function ($, csrfUrl, currentOrigin, options) {
    'use strict';

    var csrfRequest;
    var refreshRequest;
    var unauthorizedHandled = false;
    var clientOptions = options || {};

    function validateSameOriginUrl(url) {
        var baseUrl;
        var targetUrl;

        if (typeof url !== 'string' || url !== url.trim()) {
            throw new Error('ApiClient only supports valid same-origin URLs.');
        }

        try {
            baseUrl = new URL(currentOrigin);
            targetUrl = new URL(url, baseUrl);
        } catch (error) {
            throw new Error('ApiClient only supports valid same-origin URLs.');
        }

        if (!/^https?:$/.test(targetUrl.protocol) || targetUrl.origin !== baseUrl.origin) {
            throw new Error('ApiClient only supports valid same-origin URLs.');
        }
    }

    function fetchCsrf(isRefresh) {
        var request;
        validateSameOriginUrl(csrfUrl);
        request = $.getJSON(csrfUrl).then(function (response) {
            if (refreshRequest === request) {
                refreshRequest = null;
            }
            if (!response || response.code !== 'SUCCESS' || !response.data ||
                    typeof response.data.headerName !== 'string' ||
                    response.data.headerName.length === 0 ||
                    typeof response.data.token !== 'string' ||
                    response.data.token.length === 0) {
                throw new Error('CSRF response is invalid.');
            }
            return response.data;
        });
        request = request.then(null, function (error) {
            if (csrfRequest === request) {
                csrfRequest = null;
            }
            if (refreshRequest === request) {
                refreshRequest = null;
            }
            throw error;
        });
        csrfRequest = request;
        if (isRefresh) {
            refreshRequest = request;
        }
        return request;
    }

    function loadCsrf(forceRefresh) {
        if (forceRefresh) {
            return refreshRequest || fetchCsrf(true);
        }
        if (!csrfRequest) {
            return fetchCsrf(false);
        }
        return csrfRequest;
    }

    function send(options, hasRetried) {
        validateSameOriginUrl(options.url);
        return loadCsrf(false).then(function (csrf) {
            var method = (options.method || 'GET').toUpperCase();
            var headers = $.extend({}, options.headers);

            if (!/^(GET|HEAD|OPTIONS|TRACE)$/.test(method)) {
                headers[csrf.headerName] = csrf.token;
            }

            return $.ajax($.extend({}, options, {
                method: method,
                headers: headers
            }));
        }).then(null, function (request) {
            var response = request.responseJSON;
            var csrfRejected = request.status === 403 && response &&
                    response.code === 'INVALID_CSRF_TOKEN';

            if (request.status === 401 && response &&
                    response.code === 'AUTHENTICATION_REQUIRED' &&
                    !unauthorizedHandled &&
                    typeof clientOptions.onUnauthorized === 'function') {
                unauthorizedHandled = true;
                clientOptions.onUnauthorized(request);
            }

            if (csrfRejected && !hasRetried) {
                return loadCsrf(true).then(function () {
                    return send(options, true);
                });
            }

            throw request;
        });
    }

    function requestJson(method, url, body) {
        var options = {
            url: url,
            method: method,
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(body)
        };
        return send(options, false);
    }

    return {
        initialize: function () {
            return loadCsrf(false);
        },
        request: function (options) {
            return send(options, false);
        },
        get: function (url) {
            return send({url: url, method: 'GET', dataType: 'json'}, false);
        },
        post: function (url, body) {
            return requestJson('POST', url, body);
        },
        put: function (url, body) {
            return requestJson('PUT', url, body);
        },
        delete: function (url) {
            return send({url: url, method: 'DELETE', dataType: 'json'}, false);
        }
    };
}));
