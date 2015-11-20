package se.inera.intyg.webcert.web.service.diagnos.dto;

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
     * The search string is not valid.
     */
    INVALID_SEARCH_STRING,

    /**
     * The code system is not valid.
     */
    INVALID_CODE_SYSTEM,

    /**
     * A match was found using the code.
     */
    OK;
}
