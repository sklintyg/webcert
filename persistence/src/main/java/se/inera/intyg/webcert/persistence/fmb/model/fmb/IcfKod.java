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
package se.inera.intyg.webcert.persistence.fmb.model.fmb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FMB_ICF_KOD")
public class IcfKod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "KOD", nullable = false)
    private String kod;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYP", nullable = false)
    private IcfKodTyp icfKodTyp;

    protected IcfKod() {
    }

    private IcfKod(final String kod, final IcfKodTyp icfKodTyp) {
        this.kod = kod;
        this.icfKodTyp = icfKodTyp;
    }

    public Long getId() {
        return id;
    }

    public String getKod() {
        return kod;
    }

    public IcfKodTyp getIcfKodTyp() {
        return icfKodTyp;
    }


    public static final class IcfKodBuilder {

        private String kod;
        private IcfKodTyp icfKodTyp;

        private IcfKodBuilder() {
        }

        public static IcfKodBuilder anIcfKod() {
            return new IcfKodBuilder();
        }

        public IcfKodBuilder kod(String kod) {
            this.kod = kod;
            return this;
        }

        public IcfKodBuilder icfKodTyp(IcfKodTyp icfKodTyp) {
            this.icfKodTyp = icfKodTyp;
            return this;
        }

        public IcfKod build() {
            return new IcfKod(kod, icfKodTyp);
        }
    }
}
