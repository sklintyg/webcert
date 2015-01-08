package se.inera.webcert.persistence.intyg.model;

/**
 * Possible statuses for a Certificate (WIP) Entity in Webcert.
 *
 * @author marced
 */
public enum IntygsStatus {

    /**
     * Work in progress, invalid.
     */
    DRAFT_INCOMPLETE,

    /**
     * Valid and ready for signing.
     */
    DRAFT_COMPLETE,

    /**
     * Signed and valid.
     */
    SIGNED;
}
