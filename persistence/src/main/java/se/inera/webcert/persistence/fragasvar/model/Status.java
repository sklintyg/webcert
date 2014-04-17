package se.inera.webcert.persistence.fragasvar.model;

/**
 * Possible statuses for a FragaSvar Entity.
 * @author marced
 *
 */
public enum Status {
    PENDING_INTERNAL_ACTION,
    PENDING_EXTERNAL_ACTION,
    ANSWERED,
    CLOSED
}
