/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.referens.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "REFERENS")
public class Referens {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "REFERENS")
    private String referens;

    @Column(name = "INTYG_ID")
    private String intygId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferens() {
        return referens;
    }

    public void setReferens(String referens) {
        this.referens = referens;
    }

    public String getIntygsId() {
        return intygId;
    }

    public void setIntygsId(String intygsId) {
        this.intygId = intygsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Referens)) {
            return false;
        }

        Referens referens1 = (Referens) o;
        return Objects.equals(id, referens1.id)
            && Objects.equals(referens, referens1.referens)
            && Objects.equals(intygId, referens1.intygId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, referens, intygId);
    }

}
