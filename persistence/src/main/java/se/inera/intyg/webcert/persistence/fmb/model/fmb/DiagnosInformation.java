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

import com.google.common.collect.Lists;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "FMB_DIAGNOS_INFORMATION")
public final class DiagnosInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DIAGNOS_RUBRIK", nullable = true)
    private String diagnosrubrik;

    @Column(name = "FORSAKRINGSMEDICINSK_INFORMATION", nullable = false)
    private String forsakringsmedicinskInformation;

    @Column(name = "SYMPTOM_PROGNOS_BEHANDLING", nullable = false)
    private String symptomPrognosBehandling;

    @Column(name = "INFORMATION_OM_REHABILITERING", nullable = true)
    private String informationOmRehabilitering;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Beskrivning> beskrivningList = Lists.newArrayList();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Icd10Kod> icd10KodList = Lists.newArrayList();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Referens> referensList = Lists.newArrayList();

    @Column(name = "SENAST_UPPDATERAD")
    private LocalDateTime senastUppdaterad;

    protected DiagnosInformation() {
    }

    // CHECKSTYLE:OFF ParameterNumber
    private DiagnosInformation(
        final String diagnosRubrik,
        final String forsakringsmedicinskInformation,
        final String symptomPrognosBehandling,
        final String informationOmRehabilitering,
        final List<Beskrivning> beskrivningList,
        final List<Icd10Kod> icd10KodList,
        final List<Referens> referensList,
        final LocalDateTime senastUppdaterad) {
        this.diagnosrubrik = diagnosRubrik;
        this.forsakringsmedicinskInformation = forsakringsmedicinskInformation;
        this.symptomPrognosBehandling = symptomPrognosBehandling;
        this.informationOmRehabilitering = informationOmRehabilitering;
        this.beskrivningList = beskrivningList;
        this.icd10KodList = icd10KodList;
        this.referensList = referensList;
        this.senastUppdaterad = senastUppdaterad;
    }
    // CHECKSTYLE:ON ParameterNumber

    public Long getId() {
        return id;
    }

    public String getDiagnosrubrik() {
        return diagnosrubrik;
    }

    public String getForsakringsmedicinskInformation() {
        return forsakringsmedicinskInformation;
    }

    public String getSymptomPrognosBehandling() {
        return symptomPrognosBehandling;
    }

    public String getInformationOmRehabilitering() {
        return informationOmRehabilitering;
    }

    public List<Beskrivning> getBeskrivningList() {
        return beskrivningList;
    }

    public List<Icd10Kod> getIcd10KodList() {
        return icd10KodList;
    }

    public List<Referens> getReferensList() {
        return referensList;
    }

    public LocalDateTime getSenastUppdaterad() {
        return senastUppdaterad;
    }

    public static final class DiagnosInformationBuilder {

        private String diagnosRubrik;
        private String forsakringsmedicinskInformation;
        private String symptomPrognosBehandling;
        private String informationOmRehabilitering;
        private List<Beskrivning> beskrivningList = Lists.newArrayList();
        private List<Icd10Kod> icd10KodList = Lists.newArrayList();
        private List<Referens> referensList = Lists.newArrayList();
        private LocalDateTime senastUppdaterad;

        private DiagnosInformationBuilder() {
        }

        public static DiagnosInformationBuilder aDiagnosInformation() {
            return new DiagnosInformationBuilder();
        }

        public DiagnosInformationBuilder diagnosRubrik(String diagnosRubrik) {
            this.diagnosRubrik = diagnosRubrik;
            return this;
        }

        public DiagnosInformationBuilder forsakringsmedicinskInformation(String forsakringsmedicinskInformation) {
            this.forsakringsmedicinskInformation = forsakringsmedicinskInformation;
            return this;
        }

        public DiagnosInformationBuilder symptomPrognosBehandling(String symptomPrognosBehandling) {
            this.symptomPrognosBehandling = symptomPrognosBehandling;
            return this;
        }

        public DiagnosInformationBuilder informationOmRehabilitering(String informationOmRehabilitering) {
            this.informationOmRehabilitering = informationOmRehabilitering;
            return this;
        }

        public DiagnosInformationBuilder beskrivningList(List<Beskrivning> beskrivningList) {
            this.beskrivningList = beskrivningList;
            return this;
        }

        public DiagnosInformationBuilder icd10KodList(List<Icd10Kod> icd10KodList) {
            this.icd10KodList = icd10KodList;
            return this;
        }

        public DiagnosInformationBuilder referensList(List<Referens> referensList) {
            this.referensList = referensList;
            return this;
        }

        public DiagnosInformationBuilder senastUppdaterad(LocalDateTime senastUppdaterad) {
            this.senastUppdaterad = senastUppdaterad;
            return this;
        }

        public DiagnosInformation build() {
            return new DiagnosInformation(
                diagnosRubrik,
                forsakringsmedicinskInformation,
                symptomPrognosBehandling,
                informationOmRehabilitering,
                beskrivningList,
                icd10KodList,
                referensList,
                senastUppdaterad);
        }
    }
}
