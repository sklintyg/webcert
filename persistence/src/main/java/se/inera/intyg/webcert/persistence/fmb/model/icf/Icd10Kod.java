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
package se.inera.intyg.webcert.persistence.fmb.model.icf;

import com.google.common.collect.Lists;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "FMB_ICD10_KOD")
public class Icd10Kod {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "KOD", nullable = false)
    private String kod;

    @Column(name = "BESKRIVNING")
    private String beskrivning;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "ICD10_ID", nullable = false)
    private List<TypFall> typFallList = Lists.newArrayList();

    protected Icd10Kod() {
    }

    private Icd10Kod(final String kod, final String beskrivning, final List<TypFall> typFallList) {
        this.kod = kod;
        this.beskrivning = beskrivning;
        this.typFallList = typFallList;
    }

    public Long getId() {
        return id;
    }

    public String getKod() {
        return kod;
    }

    public Optional<String> getBeskrivning() {
        return Optional.ofNullable(beskrivning);
    }

    public List<TypFall> getTypFallList() {
        return typFallList;
    }

    public static final class Icd10KodBuilder {
        private String kod;
        private String beskrivning;
        private List<TypFall> typFallList = Lists.newArrayList();

        private Icd10KodBuilder() {
        }

        public static Icd10KodBuilder anIcd10Kod() {
            return new Icd10KodBuilder();
        }

        public Icd10KodBuilder kod(String kod) {
            this.kod = kod;
            return this;
        }

        public Icd10KodBuilder beskrivning(String beskrivning) {
            this.beskrivning = beskrivning;
            return this;
        }

        public Icd10KodBuilder typFallList(List<TypFall> typFallList) {
            this.typFallList = typFallList;
            return this;
        }

        public Icd10Kod build() {
            return new Icd10Kod(kod, beskrivning, typFallList);
        }
    }
}
