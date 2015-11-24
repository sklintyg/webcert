package se.inera.intyg.webcert.persistence.fragasvar.model;

/**
 * Possible statuses for a FragaSvar Entity.
 *
 * @author marced
 *
 */
public enum Status {

    /**
     * The FragaSvar has been received from an external entity and needs to be answered.
     */
    PENDING_INTERNAL_ACTION,

    /**
     * The FragaSvar has been sent to an external entity and awaits an answer.
     */
    PENDING_EXTERNAL_ACTION,

    /**
     * The FragaSvar has received an answer from the external entity.
     */
    ANSWERED,

    /**
     * The FragaSvar has been handled.
     */
    CLOSED;
}
