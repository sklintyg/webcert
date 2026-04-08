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

public class WcIntygInfo extends IntygInfo {

  private boolean createdInWC;

  private LocalDateTime draftCreated;

  private int kompletteringar;
  private int kompletteringarAnswered;

  private int administrativaFragorSent;
  private int administrativaFragorSentAnswered;

  private int administrativaFragorReceived;
  private int administrativaFragorReceivedAnswered;

  private int numberOfRecipients;

  public boolean isCreatedInWC() {
    return createdInWC;
  }

  public void setCreatedInWC(boolean createdInWC) {
    this.createdInWC = createdInWC;
  }

  public LocalDateTime getDraftCreated() {
    return draftCreated;
  }

  public void setDraftCreated(LocalDateTime draftCreated) {
    this.draftCreated = draftCreated;
  }

  public int getKompletteringar() {
    return kompletteringar;
  }

  public void setKompletteringar(int kompletteringar) {
    this.kompletteringar = kompletteringar;
  }

  public int getKompletteringarAnswered() {
    return kompletteringarAnswered;
  }

  public void setKompletteringarAnswered(int kompletteringarAnswered) {
    this.kompletteringarAnswered = kompletteringarAnswered;
  }

  public int getAdministrativaFragorSent() {
    return administrativaFragorSent;
  }

  public void setAdministrativaFragorSent(int administrativaFragorSent) {
    this.administrativaFragorSent = administrativaFragorSent;
  }

  public int getAdministrativaFragorSentAnswered() {
    return administrativaFragorSentAnswered;
  }

  public void setAdministrativaFragorSentAnswered(int administrativaFragorSentAnswered) {
    this.administrativaFragorSentAnswered = administrativaFragorSentAnswered;
  }

  public int getAdministrativaFragorReceived() {
    return administrativaFragorReceived;
  }

  public void setAdministrativaFragorReceived(int administrativaFragorReceived) {
    this.administrativaFragorReceived = administrativaFragorReceived;
  }

  public int getAdministrativaFragorReceivedAnswered() {
    return administrativaFragorReceivedAnswered;
  }

  public void setAdministrativaFragorReceivedAnswered(int administrativaFragorReceivedAnswered) {
    this.administrativaFragorReceivedAnswered = administrativaFragorReceivedAnswered;
  }

  public int getNumberOfRecipients() {
    return numberOfRecipients;
  }

  public void setNumberOfRecipients(int numberOfRecipients) {
    this.numberOfRecipients = numberOfRecipients;
  }
}
