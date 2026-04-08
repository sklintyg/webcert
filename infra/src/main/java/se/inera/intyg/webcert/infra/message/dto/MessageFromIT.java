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
package se.inera.intyg.webcert.infra.message.dto;

import java.time.LocalDateTime;

/** DTO to use when retrieving/returning messages through the internal API. */
public class MessageFromIT {

  private String certificateId;
  private String messageId;
  private String messageContent;
  private String subject;
  private String logicalAddress;
  private LocalDateTime timestamp;

  public static MessageFromIT create(
      String certificateId,
      String messageId,
      String messageContent,
      String subject,
      String logicalAddress,
      LocalDateTime timestamp) {
    final var message = new MessageFromIT();
    message.certificateId = certificateId;
    message.messageId = messageId;
    message.messageContent = messageContent;
    message.subject = subject;
    message.logicalAddress = logicalAddress;
    message.timestamp = timestamp;
    return message;
  }

  public String getCertificateId() {
    return certificateId;
  }

  public void setCertificateId(String certificateId) {
    this.certificateId = certificateId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public void setMessageContent(String messageContent) {
    this.messageContent = messageContent;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getLogicalAddress() {
    return logicalAddress;
  }

  public void setLogicalAddress(String logicalAddress) {
    this.logicalAddress = logicalAddress;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
