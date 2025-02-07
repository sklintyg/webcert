/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FMB_TYPFALL")
public class TypFall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPFALLSMENING", nullable = false)
    private String typfallsMening;

    @Column(name = "MAXIMALSJUKRIVNINGSTID_DAGAR")
    private Integer maximalSjukrivningstidDagar;

    @Column(name = "MAXIMALSJUKRIVNINGSTID_SOURCE_VALUE")
    private String maximalSjukrivningstidSourceValue;

    @Column(name = "MAXIMALSJUKRIVNINGSTID_SOURCE_UNIT")
    private String maximalSjukrivningstidSourceUnit;

    protected TypFall() {
    }

    private TypFall(final String typfallsMening, final Integer maximalSjukrivningstidDagar, final String maximalSjukrivningstidSourceValue,
        final String maximalSjukrivningstidSourceUnit) {
        this.typfallsMening = typfallsMening;
        this.maximalSjukrivningstidDagar = maximalSjukrivningstidDagar;
        this.maximalSjukrivningstidSourceValue = maximalSjukrivningstidSourceValue;
        this.maximalSjukrivningstidSourceUnit = maximalSjukrivningstidSourceUnit;
    }

    public Long getId() {
        return id;
    }

    public String getTypfallsMening() {
        return typfallsMening;
    }

    public Integer getMaximalSjukrivningstid() {
        return maximalSjukrivningstidDagar;
    }

    public String getMaximalSjukrivningstidSourceValue() {
        return maximalSjukrivningstidSourceValue;
    }

    public String getMaximalSjukrivningstidSourceUnit() {
        return maximalSjukrivningstidSourceUnit;
    }

    public static final class TypFallBuilder {

        private String typfallsMening;
        private Integer maximalSjukrivningstidDagar;
        private String maximalSjukrivningstidSourceValue;
        private String maximalSjukrivningstidSourceUnit;

        private TypFallBuilder() {
        }

        public static TypFallBuilder aTypFall() {
            return new TypFallBuilder();
        }

        public TypFallBuilder typfallsMening(String typfallsMening) {
            this.typfallsMening = typfallsMening;
            return this;
        }

        public TypFallBuilder maximalSjukrivningstidDagar(Integer maximalSjukrivningstidDagar) {
            this.maximalSjukrivningstidDagar = maximalSjukrivningstidDagar;
            return this;
        }

        public TypFallBuilder maximalSjukrivningstidSourceValue(String maximalSjukrivningstidSourceValue) {
            this.maximalSjukrivningstidSourceValue = maximalSjukrivningstidSourceValue;
            return this;
        }

        public TypFallBuilder maximalSjukrivningstidSourceUnit(String maximalSjukrivningstidSourceUnit) {
            this.maximalSjukrivningstidSourceUnit = maximalSjukrivningstidSourceUnit;
            return this;
        }

        public TypFall build() {
            return new TypFall(typfallsMening, maximalSjukrivningstidDagar, maximalSjukrivningstidSourceValue,
                maximalSjukrivningstidSourceUnit);
        }
    }
}
