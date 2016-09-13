/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.anvandarmetadata.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by eriklupander on 2016-06-22.
 *
 * Note that unique constraint is handled by liquibase DB setup.
 */
@Entity
@Table(name = "ANVANDARE_PREFERENCE")
public class AnvandarPreference {

    public static final int INT = 31;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internReferens;

    @Column(name = "HSA_ID", nullable = false)
    private String hsaId;

    @Column(name = "PREF_KEY", nullable = false)
    private String key;

    @Column(name = "PREF_VALUE", nullable = true)
    private String value;

    public AnvandarPreference() {

    }

    public AnvandarPreference(String hsaId, String key, String value) {
        this.hsaId = hsaId;
        this.key = key;
        this.value = value;
    }

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AnvandarPreference)) {
            return false;
        }


        AnvandarPreference that = (AnvandarPreference) o;

        if (!hsaId.equals(that.hsaId)) {
            return false;
        }
        if (!key.equals(that.key)) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = hsaId.hashCode();
        result = INT * result + key.hashCode();
        result = INT * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
