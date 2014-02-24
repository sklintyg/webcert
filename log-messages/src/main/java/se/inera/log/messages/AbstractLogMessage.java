package se.inera.log.messages;

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
    
    private String activityLevel;

    private ActivityType activityType;

    private LocalDateTime timestamp;

    private ActivityPurpose purpose;

    private String userId;
    private String userName;

    private Enhet enhet;

    private Patient patient;

    private String resourceType;

    /**
     * Constructor for a log message.
     * @param activityType Något av dessa värden ska anges: Läsa, Skriva, Signera, Utskrift, Vidimera, Radera och Nödöppning
     * @param purpose kan vara något av dessa värden: Vård och behandling, Kvalitetssäkring, Annan dokumentation enligt lag, Statistik, Administration och Kvalitetsregister.
     * @param resourceType Kan vara kemlabbsvar, journaltext, remiss, översikt, samtycke, patientrelation, sätta spärr, rapport osv.
     */
    public AbstractLogMessage(ActivityType activityType, ActivityPurpose purpose, String resourceType) {
        this.logId = UUID.randomUUID().toString();
        this.activityType = activityType;
        this.purpose = purpose;
        this.resourceType = resourceType;
        this.timestamp = LocalDateTime.now();
    }

    public String getLogId() {
        return logId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public ActivityPurpose getPurpose() {
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

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Enhet getEnhet() {
        return enhet;
    }

    public void setEnhet(Enhet enhet) {
        this.enhet = enhet;
    }
}
