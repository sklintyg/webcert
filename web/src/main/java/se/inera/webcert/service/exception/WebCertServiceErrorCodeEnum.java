package se.inera.webcert.service.exception;

public enum WebCertServiceErrorCodeEnum {

    INTERNAL_PROBLEM, // Generic tech problem
    INVALID_STATE, // Operation not allowed at this state, probably because of concurrency issues
    AUTHORIZATION_PROBLEM, // User is not authorized for the operation
    EXTERNAL_SYSTEM_PROBLEM, // Other system in unavailable, gave technical error response
    MODULE_PROBLEM, // Problem that occured inside a module
    UNKNOWN_INTERNAL_PROBLEM, // All others
    DATA_NOT_FOUND, //certificate (or other resource) not found
    CERTIFICATE_REVOKED,
    CONCURRENT_MODIFICATION
}
