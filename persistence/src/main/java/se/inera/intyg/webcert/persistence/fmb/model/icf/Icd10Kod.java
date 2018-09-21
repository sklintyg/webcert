package se.inera.intyg.webcert.persistence.fmb.model.icf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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

    protected Icd10Kod() {
    }

    private Icd10Kod(final String kod, final String beskrivning) {
        this.kod = kod;
        this.beskrivning = beskrivning;
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

    public static final class Icd10KodBuilder {
        private String kod;
        private String beskrivning;

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

        public Icd10Kod build() {
            return new Icd10Kod(kod, beskrivning);
        }
    }
}
