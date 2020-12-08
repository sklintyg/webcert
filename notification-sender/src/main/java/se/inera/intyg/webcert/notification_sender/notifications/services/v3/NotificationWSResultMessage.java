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

package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.NotificationResultEnum;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class NotificationWSResultMessage {

    private String certificateId;
    private String logicalAddress;
    private String userId;
    private String correlationId;
    private long messageTimestamp;
    private ResultType resultType;
    private Exception exception;
    private CertificateStatusUpdateForCareType statusUpdate;

    // these fields - or separate "resenditem"?
    //private boolean hasBeenSentBefore; if true - should expect values to be not null
    private NotificationResultEnum deliveryStatus; // default null
    //private int resendingAttempt; // default null
    //private LocalDateTime nextAttempt; // default null
    //private Handelse event;

    public CertificateStatusUpdateForCareType getStatusUpdate() {
        return statusUpdate;
    }

    public void setStatusUpdate(
        CertificateStatusUpdateForCareType statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public NotificationResultEnum getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(
        NotificationResultEnum deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /*
    public int getResendingAttempt() {
        return resendingAttempt;
    }

    public void setResendingAttempt(int resendingAttempt) {
        this.resendingAttempt = resendingAttempt;
    }

    public LocalDateTime getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(LocalDateTime nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public Handelse getEvent() {
        return event;
    }

    public void setEvent(Handelse event) {
        this.event = event;
    }

     */

    @Override
    public String toString() {
        return String
            .format("[logicalAddress: %s, certificateId: %s, correlationId: %s]", this.logicalAddress, this.certificateId,
                this.correlationId);
    }

}
