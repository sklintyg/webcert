package se.inera.webcert.service.intyg.dto;

public enum StatusType {

    /**
     * Work in progress.
     */
    DRAFT_INCOMPLETE,

    /**
     * Valid and ready for signing.
     */
    DRAFT_COMPLETE,

    /**
     * Signed and valid.
     */
    SIGNED,

    /**
     * Received and registered by Intygstjanst.
     */
    RECEIVED,

    /**
     * Sent from Intygstjanst to external recipient.
     */
    SENT,

    /**
     * Recalled and invalid.
     */
    CANCELLED,

    /**
     * Archived by citizen.
     */
    DELETED,

    /**
     * Un-archived by citizen.
     */
    RESTORED,

    /**
     * Status is unknown.
     */
    UNKNOWN;
}
