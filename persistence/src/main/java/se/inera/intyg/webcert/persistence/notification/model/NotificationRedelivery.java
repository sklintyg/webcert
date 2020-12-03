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

package se.inera.intyg.webcert.persistence.notification.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "NOTIFICATION_REDELIVERY")
public class NotificationRedelivery {

    @Id
    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "HANDELSE_ID")
    private Long eventId;

    @Column(name = "MESSAGE")
    private byte[] message;

    @Column(name = "REDELIVERY_STRATEGY")
    private String redeliveryStrategy;

    @Column(name = "REDELIVERY_TIME")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime redeliveryTime;

    @Column(name = "ATTEMPTED_REDELIVERIES")
    private int attemptedRedeliveries;

    public NotificationRedelivery() { }

    public NotificationRedelivery(String correlationId, Long eventId, byte[] message, String redeliveryStrategy,
        LocalDateTime redeliveryTime, int attemptedRedeliveries) {
        this.correlationId = correlationId;
        this.eventId = eventId;
        this.message = message;
        this.redeliveryStrategy = redeliveryStrategy;
        this.redeliveryTime = redeliveryTime;
        this.attemptedRedeliveries = attemptedRedeliveries;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public String getRedeliveryStrategy() {
        return redeliveryStrategy;
    }

    public void setRedeliveryStrategy(String redeliveryStrategy) {
        this.redeliveryStrategy = redeliveryStrategy;
    }

    public LocalDateTime getRedeliveryTime() {
        return redeliveryTime;
    }

    public void setRedeliveryTime(LocalDateTime redeliveryTime) {
        this.redeliveryTime = redeliveryTime;
    }

    public int getAttemptedRedeliveries() {
        return attemptedRedeliveries;
    }

    public void setAttemptedRedeliveries(int attemptedRedeliveries) {
        this.attemptedRedeliveries = attemptedRedeliveries;
    }
}
