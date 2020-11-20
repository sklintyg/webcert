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
@Table(name = "NOTIFICATION_RESEND")
public class NotificationResend {

    @Id
    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "HANDELSE_ID")
    private Long eventId;

    @Column(name = "RESEND_STRATEGY")
    private String resendStrategy;

    @Column(name = "RESEND_TIME")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime resendTime;

    @Column(name = "RESEND_ATTEMPTS")
    private int resendAttempts;

    public NotificationResend() { }

    public NotificationResend(String correlationId, Long eventId, String resendStrategy, LocalDateTime resendTime, int resendAttempts) {
        this.correlationId = correlationId;
        this.eventId = eventId;
        this.resendStrategy = resendStrategy;
        this.resendTime = resendTime;
        this.resendAttempts = resendAttempts;
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

    public String getResendStrategy() {
        return resendStrategy;
    }

    public void setResendStrategy(String resendStrategy) {
        this.resendStrategy = resendStrategy;
    }

    public LocalDateTime getResendTime() {
        return resendTime;
    }

    public void setResendTime(LocalDateTime resendTime) {
        this.resendTime = resendTime;
    }

    public int getResendAttempts() {
        return resendAttempts;
    }

    public void setResendAttempts(int resendAttempts) {
        this.resendAttempts = resendAttempts;
    }
}
