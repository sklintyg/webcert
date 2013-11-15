package se.inera.webcert.web.controller.moduleapi.dto;

/**
 * Simple DTO for statistics for number of unsigned certificates and unhandled questions
 * 
 * @author marced
 * 
 */
public class StatEntry {
    private int unsignedCerts;
    private int unhandledQuestions;

    public StatEntry() {
    }

    public StatEntry(int unsignedCerts, int unhandledQuestions) {
        super();
        this.unsignedCerts = unsignedCerts;
        this.unhandledQuestions = unhandledQuestions;
    }

    public int getUnsignedCerts() {
        return unsignedCerts;
    }

    public void setUnsignedCerts(int unsignedCerts) {
        this.unsignedCerts = unsignedCerts;
    }

    public int getUnhandledQuestions() {
        return unhandledQuestions;
    }

    public void setUnhandledQuestions(int unhandledQuestions) {
        this.unhandledQuestions = unhandledQuestions;
    }
}
