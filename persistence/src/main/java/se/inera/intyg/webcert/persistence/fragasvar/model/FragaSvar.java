package se.inera.intyg.webcert.persistence.fragasvar.model;

import java.util.Set;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "FRAGASVAR")
public class FragaSvar {

    /**
     * The (system-wide) unique id for this entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long internReferens;

    @Column(name = "EXTERN_REFERENS")
    private String externReferens;

    @Column(name = "FRAGE_STALLARE")
    private String frageStallare;

    @Column(name = "AMNE")
    @Enumerated(EnumType.STRING)
    private Amne amne;

    @Column(name = "FRAGE_TEXT")
    private String frageText;

    @Column(name = "FRAGE_SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime frageSigneringsDatum;

    @Column(name = "FRAGE_SKICKAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime frageSkickadDatum;

    @Column(name = "SVARS_TEXT")
    private String svarsText;

    @Column(name = "SVAR_SIGNERINGS_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime svarSigneringsDatum;

    @Column(name = "SVAR_SKICKAD_DATUM")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime svarSkickadDatum;

    @Column(name = "SENASTE_HANDELSE")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime senasteHandelse;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "EXTERNA_KONTAKTER", joinColumns = @JoinColumn(name = "FRAGASVAR_ID"))
    @Column(name = "KONTAKT")
    private Set<String> externaKontakter;

    @Column(name = "MEDDELANDE_RUBRIK")
    private String meddelandeRubrik;

    @Column(name = "SISTA_DATUM_FOR_SVAR")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate sistaDatumForSvar;

    @Column(name = "VARD_AKTOR_HSAID")
    private String vardAktorHsaId;

    @Column(name = "VARD_AKTOR_NAMN")
    private String vardAktorNamn;

    // Composites
    @Embedded
    private IntygsReferens intygsReferens;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "KOMPLETTERING", joinColumns = @JoinColumn(name = "FRAGASVAR_ID"))
    private Set<Komplettering> kompletteringar;

    @Embedded
    private Vardperson vardperson;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "VIDAREBEFORDRAD", columnDefinition = "TINYINT(1)")
    private Boolean vidarebefordrad = Boolean.FALSE;

    @PrePersist
    void onPrePersist() {
        if (getSvarSkickadDatum() == null && getFrageSkickadDatum() != null) {
            senasteHandelse = getFrageSkickadDatum();
        } else if (getSvarSkickadDatum() != null) {
            senasteHandelse = getSvarSkickadDatum();
        }
    }

    @PreUpdate
    void onPreUpdate() {
        if (getSvarSkickadDatum() != null) {
            senasteHandelse = getSvarSkickadDatum();
        }
    }

    /**
     * Tvinga laddning av refererade objeckt utan att använda FetchType.EAGER. Fixar bug WEBCERT-464 vilken duplicerade
     * Komplettering för varje externaKontakter då vi fick multipla left outer joins.
     */
    @PostLoad
    private void simulateEagerLoading() {
        getKompletteringar().size();
        getExternaKontakter().size();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(Boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public String getFrageStallare() {
        return frageStallare;
    }

    public void setFrageStallare(String frageStallare) {
        this.frageStallare = frageStallare;
    }

    public Long getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(Long internReferens) {
        this.internReferens = internReferens;
    }

    public String getExternReferens() {
        return externReferens;
    }

    public void setExternReferens(String externReferens) {
        this.externReferens = externReferens;
    }

    public Amne getAmne() {
        return amne;
    }

    public void setAmne(Amne amne) {
        this.amne = amne;
    }

    public String getFrageText() {
        return frageText;
    }

    public void setFrageText(String frageText) {
        this.frageText = frageText;
    }

    public LocalDateTime getFrageSigneringsDatum() {
        return frageSigneringsDatum;
    }

    public void setFrageSigneringsDatum(LocalDateTime frageSigneringsDatum) {
        this.frageSigneringsDatum = frageSigneringsDatum;
    }

    public LocalDateTime getFrageSkickadDatum() {
        return frageSkickadDatum;
    }

    public void setFrageSkickadDatum(LocalDateTime frageSkickadDatum) {
        this.frageSkickadDatum = frageSkickadDatum;
    }

    public String getSvarsText() {
        return svarsText;
    }

    public void setSvarsText(String svarsText) {
        this.svarsText = svarsText;
    }

    public LocalDateTime getSvarSigneringsDatum() {
        return svarSigneringsDatum;
    }

    public void setSvarSigneringsDatum(LocalDateTime svarSigneringsDatum) {
        this.svarSigneringsDatum = svarSigneringsDatum;
    }

    public LocalDateTime getSvarSkickadDatum() {
        return svarSkickadDatum;
    }

    public void setSvarSkickadDatum(LocalDateTime svarSkickadDatum) {
        this.svarSkickadDatum = svarSkickadDatum;
    }

    public Set<String> getExternaKontakter() {
        return externaKontakter;
    }

    public void setExternaKontakter(Set<String> externaKontakter) {
        this.externaKontakter = externaKontakter;
    }

    public String getMeddelandeRubrik() {
        return meddelandeRubrik;
    }

    public void setMeddelandeRubrik(String meddelandeRubrik) {
        this.meddelandeRubrik = meddelandeRubrik;
    }

    public LocalDate getSistaDatumForSvar() {
        return sistaDatumForSvar;
    }

    public void setSistaDatumForSvar(LocalDate sistaDatumForSvar) {
        this.sistaDatumForSvar = sistaDatumForSvar;
    }

    public IntygsReferens getIntygsReferens() {
        return intygsReferens;
    }

    public void setIntygsReferens(IntygsReferens intygsReferens) {
        this.intygsReferens = intygsReferens;
    }

    public Set<Komplettering> getKompletteringar() {
        return kompletteringar;
    }

    public void setKompletteringar(Set<Komplettering> kompletteringar) {
        this.kompletteringar = kompletteringar;
    }

    public Vardperson getVardperson() {
        return vardperson;
    }

    public void setVardperson(Vardperson vardperson) {
        this.vardperson = vardperson;
    }

    /**
     * Return latest event for this FragaSvar. If no svarsDatum has been set, the fragaSkickadDatum is considered the
     * lastest event.
     *
     * @return lastest date
     */
    public LocalDateTime getSenasteHandelseDatum() {
        if (svarSkickadDatum != null) {
            return svarSkickadDatum;
        } else {
            return frageSkickadDatum;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            FragaSvar fragaSvar = (FragaSvar) o;

            if (internReferens == null) {
                return fragaSvar.internReferens == null;
            } else {
                return internReferens.equals(fragaSvar.internReferens);
            }
        }
    }

    @Override
    public int hashCode() {
        return internReferens != null ? internReferens.hashCode() : 0;
    }

    public LocalDateTime getSenasteHandelse() {
        return senasteHandelse;
    }

    public String getVardAktorHsaId() {
        return vardAktorHsaId;
    }

    public void setVardAktorHsaId(String vardAktorHsaId) {
        this.vardAktorHsaId = vardAktorHsaId;
    }

    public String getVardAktorNamn() {
        return vardAktorNamn;
    }

    public void setVardAktorNamn(String vardAktorNamn) {
        this.vardAktorNamn = vardAktorNamn;
    }


}
