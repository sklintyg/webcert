package se.inera.intyg.webcert.persistence.legacy.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * Entity for a Medcert certificate migrated into Webcert.
 *
 * @author nikpet
 */
@Entity
@Table(name = "MIGRERADE_INTYG_FRAN_MEDCERT")
public class MigreratMedcertIntyg {

    @Id
    @Column(name = "INTYG_ID", nullable = false)
    private String intygsId;

    @Column(name = "ENHETS_ID", nullable = false)
    private String enhetsId;

    @Column(name = "INTYGS_TYP")
    private String intygsTyp;

    @Column(name = "URSPRUNG")
    private String ursprung;

    @Column(name = "PATIENT_NAMN", nullable = false)
    private String patientNamn;

    @Column(name = "PATIENT_SSN", nullable = false)
    private String patientPersonnummer;

    @Column(name = "SKAPAD_DATUM", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime skapad;

    @Column(name = "SKICKAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime skickad;

    @Column(name = "MIGRERAD_DATUM", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime migrerad;

    @Column(name = "MIGRERAD_FRAN", nullable = false)
    private String migreradFran;

    @Column(name = "INTYGS_DATA")
    private byte[] intygsData;

    public MigreratMedcertIntyg() {

    }

    @PrePersist
    void onPrePersist() {
        if (getMigrerad() == null) {
            setMigrerad(LocalDateTime.now());
        }
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public String getUrsprung() {
        return ursprung;
    }

    public void setUrsprung(String ursprung) {
        this.ursprung = ursprung;
    }

    public String getPatientNamn() {
        return patientNamn;
    }

    public void setPatientNamn(String patientNamn) {
        this.patientNamn = patientNamn;
    }

    public Personnummer getPatientPersonnummer() {
        return new Personnummer(patientPersonnummer);
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer.getPersonnummer();
    }

    public LocalDateTime getSkapad() {
        return skapad;
    }

    public void setSkapad(LocalDateTime skapad) {
        this.skapad = skapad;
    }

    public LocalDateTime getSkickad() {
        return skickad;
    }

    public void setSkickad(LocalDateTime skickad) {
        this.skickad = skickad;
    }

    public LocalDateTime getMigrerad() {
        return migrerad;
    }

    public void setMigrerad(LocalDateTime migrerad) {
        this.migrerad = migrerad;
    }

    public String getMigreradFran() {
        return migreradFran;
    }

    public void setMigreradFran(String migreradFran) {
        this.migreradFran = migreradFran;
    }

    public byte[] getIntygsData() {
        return intygsData;
    }

    public void setIntygsData(byte[] intygsData) {
        this.intygsData = intygsData;
    }

}
