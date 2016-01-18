/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.LocalDateTime;

/**
 * Base class for all log messages.
 *
 * @author andreaskaltenbach
 */
public class AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String RESOURCE_TYPE = "Intyg";

    private String logId;

    private String systemId;

    private String systemName;

    private String activityLevel;

    private String activityArgs;

    private ActivityType activityType;

    private LocalDateTime timestamp;

    private ActivityPurpose purpose;

    private String userId;
    private String userName;

    private Enhet userCareUnit;

    private Patient patient;

    private String resourceType;

    private Enhet resourceOwner;

    public AbstractLogMessage() {
    }

    /**
     * Constructor for a log message.
     *
     * @param activityType
     *            Något av dessa värden ska anges: Läsa, Skriva, Signera, Utskrift, Vidimera, Radera och Nödöppning
     * @param purpose
     *            kan vara något av dessa värden: Vård och behandling, Kvalitetssäkring, Annan dokumentation enligt lag,
     *            Statistik, Administration och Kvalitetsregister.
     * @param resourceType
     *            Kan vara kemlabbsvar, journaltext, remiss, översikt, samtycke, patientrelation, sätta spärr, rapport
     *            osv.
     */
    public AbstractLogMessage(ActivityType activityType, ActivityPurpose purpose, String resourceType) {
        this.logId = UUID.randomUUID().toString();
        this.activityType = activityType;
        this.purpose = purpose;
        this.resourceType = resourceType;
        this.timestamp = LocalDateTime.now();
    }

    public AbstractLogMessage(ActivityType activityType) {
        this(activityType, ActivityPurpose.CARE_TREATMENT, RESOURCE_TYPE);
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

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getActivityArgs() {
        return activityArgs;
    }

    public void setActivityArgs(String activityArgs) {
        this.activityArgs = activityArgs;
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

    public Enhet getUserCareUnit() {
        return userCareUnit;
    }

    public void setUserCareUnit(Enhet enhet) {
        this.userCareUnit = enhet;
    }

    public Enhet getResourceOwner() {
        return resourceOwner;
    }

    public void setResourceOwner(Enhet resourceOwner) {
        this.resourceOwner = resourceOwner;
    }
}
