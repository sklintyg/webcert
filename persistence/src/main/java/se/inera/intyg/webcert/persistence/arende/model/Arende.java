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
package se.inera.intyg.webcert.persistence.arende.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import se.inera.intyg.webcert.persistence.model.Status;

@Entity
@Table(name = "ARENDE")
public class Arende {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TIMESTAMP")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime timestamp;

    @Column(name = "MEDDELANDE_ID")
    private String meddelandeId;

    @Column(name = "REFERENS_ID")
    private String referensId;

    @Column(name = "SKICKAT_TIDPUNKT")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime skickatTidpunkt;

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "PATIENT_PERSON_ID")
    private String patientPersonId;

    @Column(name = "AMNE")
    @Enumerated(EnumType.STRING)
    private ArendeAmne amne;

    @Column(name = "RUBRIK")
    private String rubrik;

    @Column(name = "MEDDELANDE")
    private String meddelande;

    @Column(name = "PAMINNELSE_MEDDELANDE_ID")
    private String paminnelseMeddelandeId;

    @Column(name = "SVAR_PA_ID")
    private String svarPaId;

    @Column(name = "SVAR_PA_REFERENS")
    private String svarPaReferens;

    @Column(name = "SKICKAT_AV")
    private String skickatAv;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = "ARENDE_KONTAKT_INFO")
    @Column(name = "KONTAKT_INFO")
    private List<String> kontaktInfo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "MEDICINSKT_ARENDE", joinColumns = @JoinColumn(name = "ARENDE_ID"))
    private List<MedicinsktArende> komplettering;

    @Column(name = "SISTA_DATUM_FOR_SVAR")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDate")
    private LocalDate sistaDatumForSvar;

    @Column(name = "INTYG_TYP")
    private String intygTyp;

    @Column(name = "SIGNERAT_AV")
    private String signeratAv;

    @Column(name = "SIGNERAT_AV_NAME")
    private String signeratAvName;

    @Column(name = "ENHET")
    private String enhetId;

    @Column(name = "ENHET_NAME")
    private String enhetName;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "SENASTE_HANDELSE")
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime senasteHandelse;

    @Column(name = "VIDAREBEFORDRAD", columnDefinition = "TINYINT(1)")
    private Boolean vidarebefordrad = Boolean.FALSE;

    @Column(name = "VARDAKTOR_NAME")
    private String vardaktorName;

    @Column(name = "VARDGIVARE_NAME")
    private String vardgivareName;

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

    public String getMeddelandeId() {
        return meddelandeId;
    }

    public void setMeddelandeId(String meddelandeId) {
        this.meddelandeId = meddelandeId;
    }

    public String getReferensId() {
        return referensId;
    }

    public void setReferensId(String referensId) {
        this.referensId = referensId;
    }

    public LocalDateTime getSkickatTidpunkt() {
        return skickatTidpunkt;
    }

    public void setSkickatTidpunkt(LocalDateTime skickatTidpunkt) {
        this.skickatTidpunkt = skickatTidpunkt;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getPatientPersonId() {
        return patientPersonId;
    }

    public void setPatientPersonId(String patientPersonId) {
        this.patientPersonId = patientPersonId;
    }

    public ArendeAmne getAmne() {
        return amne;
    }

    public void setAmne(ArendeAmne amne) {
        this.amne = amne;
    }

    public String getRubrik() {
        return rubrik;
    }

    public void setRubrik(String rubrik) {
        this.rubrik = rubrik;
    }

    public String getMeddelande() {
        return meddelande;
    }

    public void setMeddelande(String meddelande) {
        this.meddelande = meddelande;
    }

    public String getPaminnelseMeddelandeId() {
        return paminnelseMeddelandeId;
    }

    public void setPaminnelseMeddelandeId(String paminnelseMeddelandeId) {
        this.paminnelseMeddelandeId = paminnelseMeddelandeId;
    }

    public String getSkickatAv() {
        return skickatAv;
    }

    public void setSkickatAv(String skickatAv) {
        this.skickatAv = skickatAv;
    }

    public List<MedicinsktArende> getKomplettering() {
        if (komplettering == null) {
            komplettering = new ArrayList<>();
        }
        return komplettering;
    }

    public LocalDate getSistaDatumForSvar() {
        return sistaDatumForSvar;
    }

    public void setSistaDatumForSvar(LocalDate sistaDatumForSvar) {
        this.sistaDatumForSvar = sistaDatumForSvar;
    }

    public List<String> getKontaktInfo() {
        if (kontaktInfo == null) {
            kontaktInfo = new ArrayList<>();
        }
        return kontaktInfo;
    }

    public String getSvarPaId() {
        return svarPaId;
    }

    public void setSvarPaId(String svarPaId) {
        this.svarPaId = svarPaId;
    }

    public String getSvarPaReferens() {
        return svarPaReferens;
    }

    public void setSvarPaReferens(String svarPaReferens) {
        this.svarPaReferens = svarPaReferens;
    }

    public String getIntygTyp() {
        return intygTyp;
    }

    public void setIntygTyp(String intygTyp) {
        this.intygTyp = intygTyp;
    }

    public String getSigneratAv() {
        return signeratAv;
    }

    public void setSigneratAv(String signeratAv) {
        this.signeratAv = signeratAv;
    }

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public String getEnhetName() {
        return enhetName;
    }

    public void setEnhetName(String enhetName) {
        this.enhetName = enhetName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setKomplettering(List<MedicinsktArende> kompletteringar) {
        this.komplettering = kompletteringar;
    }

    public String getSigneratAvName() {
        return signeratAvName;
    }

    public void setSigneratAvName(String signeratAvName) {
        this.signeratAvName = signeratAvName;
    }

    public LocalDateTime getSenasteHandelse() {
        return senasteHandelse;
    }

    public void setSenasteHandelse(LocalDateTime senasteHandelse) {
        this.senasteHandelse = senasteHandelse;
    }

    public Boolean getVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(Boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public void setArendeToVidareBerordrat() {
        this.vidarebefordrad = true;
    }

    public String getVardaktorName() {
        return vardaktorName;
    }

    public void setVardaktorName(String vardaktorName) {
        this.vardaktorName = vardaktorName;
    }

    public String getVardgivareName() {
        return vardgivareName;
    }

    public void setVardgivareName(String vardgivareName) {
        this.vardgivareName = vardgivareName;
    }
}
