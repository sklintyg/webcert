package se.inera.webcert.persistence;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

@Entity
@Table(name = "FRAGASVAR")
public class FragaSvar {

    /**
     * The (system-wide) unique id for this entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String internReferens;

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

    @ElementCollection
    @CollectionTable(name = "EXTERNA_KONTAKTER", joinColumns = @JoinColumn(name = "FRAGASVAR_ID"))
    @Column(name = "KONTAKT")
    private List<String> externaKontakter;

    @Column(name = "MEDDELANDE_RUBRIK")
    private String meddelandeRubrik;
    
    @Column(name = "SISTA_DATUM_FOR_SVAR")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate sistaDatumForSvar;

    // Composites
    @Embedded
    private IntygsReferens intygsReferens;

    @ElementCollection
    @CollectionTable(name = "KOMPLETTERING", joinColumns = @JoinColumn(name = "FRAGASVAR_ID"))
    private List<Komplettering> kompletteringar;

    @Embedded
    private Vardperson vardperson;

    public String getFrageStallare() {
        return frageStallare;
    }

    public void setFrageStallare(String frageStallare) {
        this.frageStallare = frageStallare;
    }

    public String getInternReferens() {
        return internReferens;
    }

    public void setInternReferens(String internReferens) {
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

    public List<String> getExternaKontakter() {
        return externaKontakter;
    }

    public void setExternaKontakter(List<String> externaKontakter) {
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

    public List<Komplettering> getKompletteringar() {
        return kompletteringar;
    }

    public void setKompletteringar(List<Komplettering> kompletteringar) {
        this.kompletteringar = kompletteringar;
    }

    public Vardperson getVardperson() {
        return vardperson;
    }

    public void setVardperson(Vardperson vardperson) {
        this.vardperson = vardperson;
    }

}
