/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.intyginfo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class IntygInfo {

  private String intygId;
  private String intygType;
  private String intygVersion;

  private LocalDateTime signedDate;
  private LocalDateTime sentToRecipient;

  private List<IntygInfoEvent> events = new ArrayList<>();

  private String signedByName;
  private String signedByHsaId;
  private String signedByEmail;

  private String careUnitName;
  private String careUnitHsaId;

  private String careGiverName;
  private String careGiverHsaId;

  private boolean testCertificate;

  public String getIntygId() {
    return intygId;
  }

  public void setIntygId(String intygId) {
    this.intygId = intygId;
  }

  public String getIntygType() {
    return intygType;
  }

  public void setIntygType(String intygType) {
    this.intygType = intygType;
  }

  public String getIntygVersion() {
    return intygVersion;
  }

  public void setIntygVersion(String intygVersion) {
    this.intygVersion = intygVersion;
  }

  public LocalDateTime getSignedDate() {
    return signedDate;
  }

  public void setSignedDate(LocalDateTime signedDate) {
    this.signedDate = signedDate;
  }

  public LocalDateTime getSentToRecipient() {
    return sentToRecipient;
  }

  public void setSentToRecipient(LocalDateTime sentToRecipient) {
    this.sentToRecipient = sentToRecipient;
  }

  public List<IntygInfoEvent> getEvents() {
    return events;
  }

  public void setEvents(List<IntygInfoEvent> events) {
    this.events = events;
  }

  public String getSignedByName() {
    return signedByName;
  }

  public void setSignedByName(String signedByName) {
    this.signedByName = signedByName;
  }

  public String getSignedByHsaId() {
    return signedByHsaId;
  }

  public void setSignedByHsaId(String signedByHsaId) {
    this.signedByHsaId = signedByHsaId;
  }

  public String getSignedByEmail() {
    return signedByEmail;
  }

  public void setSignedByEmail(String signedByEmail) {
    this.signedByEmail = signedByEmail;
  }

  public String getCareUnitName() {
    return careUnitName;
  }

  public void setCareUnitName(String careUnitName) {
    this.careUnitName = careUnitName;
  }

  public String getCareUnitHsaId() {
    return careUnitHsaId;
  }

  public void setCareUnitHsaId(String careUnitHsaId) {
    this.careUnitHsaId = careUnitHsaId;
  }

  public String getCareGiverName() {
    return careGiverName;
  }

  public void setCareGiverName(String careGiverName) {
    this.careGiverName = careGiverName;
  }

  public String getCareGiverHsaId() {
    return careGiverHsaId;
  }

  public void setCareGiverHsaId(String careGiverHsaId) {
    this.careGiverHsaId = careGiverHsaId;
  }

  public boolean isTestCertificate() {
    return testCertificate;
  }

  public void setTestCertificate(boolean isTestCertificate) {
    this.testCertificate = isTestCertificate;
  }
}
