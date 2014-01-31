package se.inera.webcert.persistence.intyg.model;

/**
 * Possible statuses for a Certificate (WIP) Entity in Webcert
 * @author marced
 *
 */
public enum IntygsStatus {
    DRAFT_INCOMPLETE,
    DRAFT_COMPLETE,
    DRAFT_DISCARDED,
    WORK_IN_PROGRESS, 
    SIGNED
}
