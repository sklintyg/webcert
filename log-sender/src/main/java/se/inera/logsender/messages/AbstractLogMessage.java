package se.inera.logsender.messages;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.LocalDateTime;

/**
 * Base class for all log messages.
 *
 * @author andreaskaltenbach
 */
public class AbstractLogMessage implements Serializable {

    private String logId;

    private String systemId;

    private String activityType;

    private LocalDateTime timestamp;

    private String purpose;

    private String userId;
    private String enhetId;
    private String vardgivareId;

    private String resourceType;

    /**
     * Constructor for a log message.
     * @param activityType Något av dessa värden ska anges: Läsa, Skriva, Signera, Utskrift, Vidimera, Radera och Nödöppning
     * @param purpose kan vara något av dessa värden: Vård och behandling, Kvalitetssäkring, Annan dokumentation enligt lag, Statistik, Administration och Kvalitetsregister.
     * @param resourceType Kan vara kemlabbsvar, journaltext, remiss, översikt, samtycke, patientrelation, sätta spärr, rapport osv.
     */
    public AbstractLogMessage(String activityType, String purpose, String resourceType) {
        this.logId = UUID.randomUUID().toString();
        this.activityType = activityType;
        this.purpose = purpose;
        this.resourceType = resourceType;
    }

    public String getLogId() {
        return logId;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public String getVardgivareId() {
        return vardgivareId;
    }

    public void setVardgivareId(String vardgivareId) {
        this.vardgivareId = vardgivareId;
    }
}
