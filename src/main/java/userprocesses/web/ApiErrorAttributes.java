package userprocesses.web;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import userprocesses.web.response.ApiCode;

@Component
public final class ApiErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(
            WebRequest webRequest, ErrorAttributeOptions options) {
        int status = status(webRequest);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", code(status));
        response.put("msg", message(status));
        response.put("data", null);
        return response;
    }

    private int status(WebRequest webRequest) {
        Object value = webRequest.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE, RequestAttributes.SCOPE_REQUEST);
        return value instanceof Integer ? (Integer) value : 500;
    }

    private ApiCode code(int status) {
        switch (status) {
            case 400:
                return ApiCode.INVALID_REQUEST;
            case 401:
                return ApiCode.AUTHENTICATION_REQUIRED;
            case 403:
                return ApiCode.ACCESS_DENIED;
            case 404:
                return ApiCode.RESOURCE_NOT_FOUND;
            case 405:
                return ApiCode.METHOD_NOT_ALLOWED;
            case 415:
                return ApiCode.UNSUPPORTED_MEDIA_TYPE;
            default:
                return ApiCode.INTERNAL_SERVER_ERROR;
        }
    }

    private String message(int status) {
        switch (status) {
            case 400:
                return "Request is invalid.";
            case 401:
                return "Authentication is required.";
            case 403:
                return "Access is denied.";
            case 404:
                return "Resource not found.";
            case 405:
                return "Request method is not supported.";
            case 415:
                return "Content-Type is not supported.";
            default:
                return "An unexpected error occurred.";
        }
    }
}
