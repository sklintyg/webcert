/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.privatlakaravtal.model;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.Type;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Entity
@Table(name = "AVTAL_PRIVATLAKARE")
public class Avtal {

    @Id
    @Column(name = "AVTAL_VERSION")
    private Integer avtalVersion;

    @Lob
    @Column(name = "AVTAL_TEXT")
    private String avtalText;

    @Column(name = "VERSION_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime versionDatum;

    public String getAvtalText() {
        return avtalText;
    }

    public void setAvtalText(String avtalText) {
        this.avtalText = avtalText;
    }

    public Integer getAvtalVersion() {
        return avtalVersion;
    }

    public void setAvtalVersion(Integer avtalVersion) {
        this.avtalVersion = avtalVersion;
    }

    public LocalDateTime getVersionDatum() {
        return versionDatum;
    }

    public void setVersionDatum(LocalDateTime versionDatum) {
        this.versionDatum = versionDatum;
    }
}
