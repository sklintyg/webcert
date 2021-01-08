/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;


public class NotificationResultMessage {

    private String certificateId;
    private String logicalAddress;
    private String correlationId;
    private Handelse event;
    private Boolean isFailedMessage;
    private Boolean isManualRedelivery;
    private NotificationResultType resultType;
    private ExceptionInfoMessage exceptionInfoMessage;
    private NotificationRedeliveryMessage notificationRedeliveryMessage;

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Handelse getEvent() {
        return event;
    }

    public void setEvent(Handelse event) {
        this.event = event;
    }

    public Boolean getIsFailedMessage() {
        return isFailedMessage;
    }

    public void setIsFailedMessage(Boolean isFailedMessage) {
        this.isFailedMessage = isFailedMessage;
    }

    public Boolean getIsManualRedelivery() {
        return isManualRedelivery;
    }

    public void setIsManualRedelivery(Boolean isManualRedelivery) {
        this.isManualRedelivery = isManualRedelivery;
    }

    public NotificationResultType getResultType() {
        return resultType;
    }

    public void setResultType(NotificationResultType resultType) {
        this.resultType = resultType;
    }

    public ExceptionInfoMessage getExceptionInfoMessage() {
        return exceptionInfoMessage;
    }

    public void setExceptionInfoMessage(ExceptionInfoMessage exceptionInfoMessage) {
        this.exceptionInfoMessage = exceptionInfoMessage;
    }

    public NotificationRedeliveryMessage getNotificationRedeliveryMessage() {
        return notificationRedeliveryMessage;
    }

    public void setNotificationRedeliveryMessage(
        NotificationRedeliveryMessage notificationRedeliveryMessage) {
        this.notificationRedeliveryMessage = notificationRedeliveryMessage;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("[logicalAddress: %s, certificateId: %s, correlationId: %s]", this.logicalAddress, this.certificateId,
            this.correlationId);
    }

}
