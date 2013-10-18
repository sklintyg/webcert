package se.inera.webcert.service.exception;

public enum WebCertServiceErrorCodeEnum {

    INTERNAL_PROBLEM, // Generic tech problem
    INVALID_STATE, // Operation not allowed at this state, probably because of concurrency issues
    AUTHORIZATION_PROBLEM, // User is not authorized for the operation
    EXTERNAL_SYSTEM_PROBLEM, // Other system in unavailable, gave technical error response
    UNKNOWN_INTERNAL_PROBLEM // All others
}
