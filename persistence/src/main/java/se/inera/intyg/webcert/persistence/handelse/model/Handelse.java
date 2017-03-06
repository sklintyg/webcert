package se.inera.intyg.webcert.persistence.handelse.model;

import org.hibernate.annotations.Type;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "HANDELSE")
public class Handelse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TIMESTAMP")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime timestamp;

    @Column(name = "KOD")
    @Enumerated(EnumType.STRING)
    private HandelsekodEnum code;

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "ENHETS_ID")
    private String enhetsId;

    @Column(name = "VARDGIVAR_ID")
    private String vardgivarId;

    @Column(name = "PATIENT_PERSON_ID")
    private String personnummer;

    @Column(name = "REFERENCE")
    private String ref;

    public Handelse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public HandelsekodEnum getCode() {
        return code;
    }

    public void setCode(HandelsekodEnum code) {
        this.code = code;
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

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(String patientPersonId) {
        this.personnummer = patientPersonId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
