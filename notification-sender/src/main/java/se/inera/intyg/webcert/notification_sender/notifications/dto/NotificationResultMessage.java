/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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


public class NotificationResultMessage {

    private String correlationId;
    private Handelse event;
    private NotificationResultType resultType;
    private byte[] redeliveryMessageBytes;


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

    public NotificationResultType getResultType() {
        return resultType;
    }

    public void setResultType(NotificationResultType resultType) {
        this.resultType = resultType;
    }

    public byte[] getRedeliveryMessageBytes() {
        return redeliveryMessageBytes;
    }

    public void setRedeliveryMessageBytes(byte[] redeliveryMessageBytes) {
        this.redeliveryMessageBytes = redeliveryMessageBytes;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("[logicalAddress: %s, certificateId: %s, correlationId: %s, eventCode: %s]", this.event.getEnhetsId(),
            this.event.getIntygsId(), this.correlationId, this.event.getCode() != null ? this.event.getCode().name() : null);
    }

}
