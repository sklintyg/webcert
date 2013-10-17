package se.inera.webcert.web.controller.moduleapi.dto;

import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;

public class ModuleRestExceptionResponse {

    private WebCertServiceErrorCodeEnum errorCode;
    private String message;

    public ModuleRestExceptionResponse() {
    }

    public ModuleRestExceptionResponse(WebCertServiceErrorCodeEnum errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public WebCertServiceErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(WebCertServiceErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
