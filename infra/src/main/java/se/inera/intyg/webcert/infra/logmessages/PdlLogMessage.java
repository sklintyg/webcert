/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.logmessages;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.Enhet;
import se.inera.intyg.infra.logmessages.PdlResource;

/**
 * Class for all log messages. Each project sending PDL log messsages should implement their own
 * domain-specific factories for producing PdlLogMessage instances having the relevant properties.
 *
 * @author eriklupander
 */
public class PdlLogMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private String logId;
  private String systemId;
  private String systemName;

  private String activityLevel;
  private String activityArgs;
  private ActivityType activityType;
  private ActivityPurpose purpose;

  private LocalDateTime timestamp;

  private String userId;
  private String userName;
  private String userTitle;
  private String userAssignment;
  private Enhet userCareUnit;

  private List<PdlResource> pdlResourceList;

  public PdlLogMessage() {
    this.logId = UUID.randomUUID().toString();
  }

  public PdlLogMessage(String logId) {
    this.logId = logId;
  }

  public PdlLogMessage(ActivityType activityType) {
    this(activityType, ActivityPurpose.CARE_TREATMENT);
  }

  public PdlLogMessage(ActivityType activityType, ActivityPurpose activityPurpose) {
    this(activityType, activityPurpose, new ArrayList<>());
  }

  /**
   * Constructor for a log message.
   *
   * @param activityType Något av dessa värden ska anges: Läsa, Skriva, Signera, Utskrift, Vidimera,
   *     Radera och Nödöppning
   * @param purpose kan vara något av dessa värden: Vård och behandling, Kvalitetssäkring, Annan
   *     dokumentation enligt lag, Statistik, Administration och Kvalitetsregister.
   * @param pdlResourceList Kan vara kemlabbsvar, journaltext, remiss, översikt, samtycke,
   *     patientrelation, sätta spärr, rapport, Översikt sjukskrivning osv.
   */
  public PdlLogMessage(
      ActivityType activityType, ActivityPurpose purpose, List<PdlResource> pdlResourceList) {
    this.logId = UUID.randomUUID().toString();
    this.activityType = activityType;
    this.purpose = purpose;
    this.pdlResourceList = pdlResourceList;
    this.timestamp = LocalDateTime.now();
  }

  public List<PdlResource> getPdlResourceList() {
    if (pdlResourceList == null) {
      pdlResourceList = new ArrayList<>();
    }
    return pdlResourceList;
  }

  public String getLogId() {
    return logId;
  }

  public void setPdlResourceList(List<PdlResource> pdlResourceList) {
    this.pdlResourceList = pdlResourceList;
  }

  public Enhet getUserCareUnit() {
    return userCareUnit;
  }

  public void setUserCareUnit(Enhet userCareUnit) {
    this.userCareUnit = userCareUnit;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ActivityPurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(ActivityPurpose purpose) {
    this.purpose = purpose;
  }

  public ActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public String getActivityArgs() {
    return activityArgs;
  }

  public void setActivityArgs(String activityArgs) {
    this.activityArgs = activityArgs;
  }

  public String getActivityLevel() {
    return activityLevel;
  }

  public void setActivityLevel(String activityLevel) {
    this.activityLevel = activityLevel;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String getUserTitle() {
    return userTitle;
  }

  public void setUserTitle(String userTitle) {
    this.userTitle = userTitle;
  }

  public String getUserAssignment() {
    return userAssignment;
  }

  public void setUserAssignment(String userAssignment) {
    this.userAssignment = userAssignment;
  }

  /**
   * Returns a copy (new instance) of this, optionally omitting the resourceList.
   *
   * @return A brand new instance of PdlLogMessage.
   */
  public PdlLogMessage copy(boolean includeResourceList) {
    PdlLogMessage msg = new PdlLogMessage(this.activityType, this.purpose);
    msg.setActivityArgs(this.activityArgs);
    msg.setActivityLevel(this.activityLevel);
    msg.setSystemId(this.systemId);
    msg.setSystemName(this.systemName);
    msg.setTimestamp(this.timestamp);
    msg.setUserCareUnit(this.userCareUnit);
    msg.setUserId(this.userId);
    msg.setUserName(this.userName);
    msg.setUserAssignment(this.userAssignment);
    msg.setUserTitle(this.userTitle);

    if (includeResourceList) {
      msg.setPdlResourceList(pdlResourceList);
    }

    return msg;
  }
}
