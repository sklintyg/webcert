package se.inera.intyg.webcert.persistence.fmb.model.icf;

import com.google.common.collect.Lists;
import org.hibernate.annotations.Type;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "FMB_DIAGNOS_INFORMATION")
public final class DiagnosInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FORSAKRINGSMEDICINSK_INFORMATION", nullable = false)
    private String forsakringsmedicinskInformation;

    @Column(name = "SYMPTOM_PROGNOS_BEHANDLING", nullable = false)
    private String symptomPrognosBehandling;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Beskrivning> beskrivningList = Lists.newArrayList();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<TypFall> typFallList = Lists.newArrayList();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Icd10Kod> icd10KodList = Lists.newArrayList();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DIAGNOS_INFORMATION_ID", nullable = false)
    private List<Referens> referensList = Lists.newArrayList();

    @Column(name = "SENAST_UPPDATERAD")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime senastUppdaterad;

    protected DiagnosInformation() {
    }

    private DiagnosInformation(
            final String forsakringsmedicinskInformation,
            final String symptomPrognosBehandling,
            final List<Beskrivning> beskrivningList,
            final List<TypFall> typFallList,
            final List<Icd10Kod> icd10KodList,
            final List<Referens> referensList,
            final LocalDateTime senastUppdaterad) {
        this.forsakringsmedicinskInformation = forsakringsmedicinskInformation;
        this.symptomPrognosBehandling = symptomPrognosBehandling;
        this.beskrivningList = beskrivningList;
        this.typFallList = typFallList;
        this.icd10KodList = icd10KodList;
        this.referensList = referensList;
        this.senastUppdaterad = senastUppdaterad;
    }

    public Long getId() {
        return id;
    }

    public String getForsakringsmedicinskInformation() {
        return forsakringsmedicinskInformation;
    }

    public String getSymptomPrognosBehandling() {
        return symptomPrognosBehandling;
    }

    public List<Beskrivning> getBeskrivningList() {
        return beskrivningList;
    }

    public List<TypFall> getTypFallList() {
        return typFallList;
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
        private String forsakringsmedicinskInformation;
        private String symptomPrognosBehandling;
        private List<Beskrivning> beskrivningList = Lists.newArrayList();
        private List<TypFall> typFallList = Lists.newArrayList();
        private List<Icd10Kod> icd10KodList = Lists.newArrayList();
        private List<Referens> referensList = Lists.newArrayList();
        private LocalDateTime senastUppdaterad;

        private DiagnosInformationBuilder() {
        }

        public static DiagnosInformationBuilder aDiagnosInformation() {
            return new DiagnosInformationBuilder();
        }

        public DiagnosInformationBuilder forsakringsmedicinskInformation(String forsakringsmedicinskInformation) {
            this.forsakringsmedicinskInformation = forsakringsmedicinskInformation;
            return this;
        }

        public DiagnosInformationBuilder symptomPrognosBehandling(String symptomPrognosBehandling) {
            this.symptomPrognosBehandling = symptomPrognosBehandling;
            return this;
        }

        public DiagnosInformationBuilder beskrivningList(List<Beskrivning> beskrivningList) {
            this.beskrivningList = beskrivningList;
            return this;
        }

        public DiagnosInformationBuilder typFallList(List<TypFall> typFallList) {
            this.typFallList = typFallList;
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
                    forsakringsmedicinskInformation,
                    symptomPrognosBehandling,
                    beskrivningList,
                    typFallList,
                    icd10KodList,
                    referensList,
                    senastUppdaterad);
        }
    }
}
