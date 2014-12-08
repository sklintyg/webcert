package se.inera.webcert.service.diagnos.dto;

public enum DiagnosResponseType {

    /**
     * A Diagnosis matching the code was not found.
     */
    NOT_FOUND,

    /**
     * The supplied code is not valid.
     */
    INVALID_CODE,

    /**
     * A match was found using the code.
     */
    OK;
}
