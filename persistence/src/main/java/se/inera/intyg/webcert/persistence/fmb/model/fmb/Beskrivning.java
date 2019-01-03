/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import com.google.common.collect.Lists;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "FMB_BESKRIVNING")
public class Beskrivning {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYP", nullable = false)
    private BeskrivningTyp beskrivningTyp;

    @Column(name = "BESKRIVNING", nullable = false)
    private String beskrivningText;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "BESKRIVNING_ID", nullable = false)
    private List<IcfKod> icfKodList = Lists.newArrayList();

    protected Beskrivning() {
    }

    public Beskrivning(
            final BeskrivningTyp beskrivningTyp,
            final String beskrivningText,
            final List<IcfKod> icfKodList) {
        this.beskrivningTyp = beskrivningTyp;
        this.beskrivningText = beskrivningText;
        this.icfKodList = icfKodList;
    }

    public Long getId() {
        return id;
    }

    public BeskrivningTyp getBeskrivningTyp() {
        return beskrivningTyp;
    }

    public String getBeskrivningText() {
        return beskrivningText;
    }

    public List<IcfKod> getIcfKodList() {
        return icfKodList;
    }

    public static final class BeskrivningBuilder {
        private BeskrivningTyp beskrivningTyp;
        private String beskrivningText;
        private List<IcfKod> icfKodList;

        private BeskrivningBuilder() {
        }

        public static BeskrivningBuilder aBeskrivning() {
            return new BeskrivningBuilder();
        }

        public BeskrivningBuilder beskrivningTyp(BeskrivningTyp beskrivningTyp) {
            this.beskrivningTyp = beskrivningTyp;
            return this;
        }

        public BeskrivningBuilder beskrivningText(String beskrivningText) {
            this.beskrivningText = beskrivningText;
            return this;
        }

        public BeskrivningBuilder icfKodList(List<IcfKod> icfKodList) {
            this.icfKodList = icfKodList;
            return this;
        }

        public Beskrivning build() {
            return new Beskrivning(beskrivningTyp, beskrivningText, icfKodList);
        }
    }
}
