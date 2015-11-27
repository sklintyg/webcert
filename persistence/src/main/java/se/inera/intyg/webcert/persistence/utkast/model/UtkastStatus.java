package se.inera.intyg.webcert.persistence.utkast.model;

/**
 * Possible statuses for a Utkast entity in Webcert.
 *
 * @author marced
 */
public enum UtkastStatus {

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
