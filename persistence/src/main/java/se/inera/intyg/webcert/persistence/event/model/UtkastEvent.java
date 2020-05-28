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

package se.inera.intyg.webcert.persistence.event.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.inera.intyg.common.support.common.enumerations.EventKod;

@Entity
@Table(name = "INTYG_EVENT")
public class UtkastEvent {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "ANVANDARE")
    private String anvandare;

    @Column(name = "EVENT_KOD")
    @Enumerated(EnumType.STRING)
    private EventKod eventKod;

    @Column(name = "TIMESTAMP")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime timestamp;

    @Column(name = "MEDDELANDE")
    private String meddelande;

    public UtkastEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntygsId() {
        return this.intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getAnvandare() {
        return anvandare;
    }

    public void setAnvandare(String anvandare) {
        this.anvandare = anvandare;
    }

    public EventKod getEventKod() {
        return eventKod;
    }

    public void setEventKod(EventKod eventKod) {
        this.eventKod = eventKod;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeddelande() {
        return meddelande;
    }

    public void setMeddelande(String meddelande) {
        this.meddelande = meddelande;
    }
}
