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

import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class NotificationWSResultMessage {

    private String certificateId;
    private String correlationId;
    private String logicalAddress;
    private String userId;
    private ResultType resultType;
    private Exception exception;
    private CertificateStatusUpdateForCareType statusMessage;

    public NotificationWSResultMessage() { }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public CertificateStatusUpdateForCareType getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(CertificateStatusUpdateForCareType statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return String.format("[logicalAddress: %s, intygId: %s, correlationId: %s]", logicalAddress, certificateId, correlationId);
    }
}