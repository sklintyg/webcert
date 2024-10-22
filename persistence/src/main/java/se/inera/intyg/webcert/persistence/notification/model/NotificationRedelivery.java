/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;

@Entity
@Table(name = "NOTIFICATION_REDELIVERY")
public class NotificationRedelivery {

    @Id
    @Column(name = "HANDELSE_ID")
    private Long eventId;

    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "MESSAGE")
    private byte[] message;

    @Column(name = "REDELIVERY_STRATEGY")
    @Enumerated(EnumType.STRING)
    private NotificationRedeliveryStrategyEnum redeliveryStrategy;

    // Jadira?
    @Column(name = "REDELIVERY_TIME")
    private LocalDateTime redeliveryTime;

    @Column(name = "ATTEMPTED_DELIVERIES")
    private Integer attemptedDeliveries;

    public NotificationRedelivery() {
    }

    public NotificationRedelivery(String correlationId, Long eventId, byte[] message, NotificationRedeliveryStrategyEnum redeliveryStrategy,
        LocalDateTime redeliveryTime, Integer attemptedDeliveries) {
        this.correlationId = correlationId;
        this.eventId = eventId;
        this.message = message;
        this.redeliveryStrategy = redeliveryStrategy;
        this.redeliveryTime = redeliveryTime;
        this.attemptedDeliveries = attemptedDeliveries;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public NotificationRedeliveryStrategyEnum getRedeliveryStrategy() {
        return redeliveryStrategy;
    }

    public void setRedeliveryStrategy(NotificationRedeliveryStrategyEnum redeliveryStrategy) {
        this.redeliveryStrategy = redeliveryStrategy;
    }

    public LocalDateTime getRedeliveryTime() {
        return redeliveryTime;
    }

    public void setRedeliveryTime(LocalDateTime redeliveryTime) {
        this.redeliveryTime = redeliveryTime;
    }

    public Integer getAttemptedDeliveries() {
        return attemptedDeliveries;
    }

    public void setAttemptedDeliveries(Integer attemptedRedeliveries) {
        this.attemptedDeliveries = attemptedRedeliveries;
    }
}
