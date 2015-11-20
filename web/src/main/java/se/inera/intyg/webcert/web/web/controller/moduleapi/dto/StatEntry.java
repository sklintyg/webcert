package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

/**
 * Simple DTO for statistics for number of unsigned certificates and unhandled questions.
 *
 * @author marced
 */
public class StatEntry {
    private long unsignedCerts;
    private long unhandledQuestions;

    public StatEntry() {
    }

    public StatEntry(long unsignedCerts, long unhandledQuestions) {
        super();
        this.unsignedCerts = unsignedCerts;
        this.unhandledQuestions = unhandledQuestions;
    }

    public long getUnsignedCerts() {
        return unsignedCerts;
    }

    public void setUnsignedCerts(long unsignedCerts) {
        this.unsignedCerts = unsignedCerts;
    }

    public long getUnhandledQuestions() {
        return unhandledQuestions;
    }

    public void setUnhandledQuestions(long unhandledQuestions) {
        this.unhandledQuestions = unhandledQuestions;
    }
}
